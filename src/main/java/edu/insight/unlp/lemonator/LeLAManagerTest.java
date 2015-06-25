package edu.insight.unlp.lemonator;

import eu.monnetproject.lang.Language;
import eu.monnetproject.lemon.generator.*;
import eu.monnetproject.lemon.generator.lela.ActorGenerationReportImpl;
import eu.monnetproject.lemon.generator.lela.EntryGenerationReportImpl;
import eu.monnetproject.lemon.generator.lela.GenerationReportImpl;
//import eu.monnetproject.lemon.generator.lela.LabelExtractorActor;
import eu.monnetproject.lemon.*;
import eu.monnetproject.label.*;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import eu.monnetproject.lemon.model.Text;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;

import java.net.*;
import java.util.*;

import eu.monnetproject.util.*;
import net.lexinfo.LexInfo;

/**
 * The manager that controls the generation process
 *
 * @author John McCrae
 */
public class LeLAManagerTest implements LemonGenerator {

	final TreeSet<GeneratorActor> actors;
	private final HashMap<GeneratorActor, Double> actorPriority = new HashMap<GeneratorActor, Double>();
	//private List<LabelExtractor> labelExtractors = new LinkedList<LabelExtractor>();
	//private List<LabelExtractor> labelExtractorsWithoutDefault = new LinkedList<LabelExtractor>();
	//private ComponentFactory customLabelExtractor = null;
	//private LabelExtractorActor labelExtractorActorWithoutDefault = new LabelExtractorActor(labelExtractorsWithoutDefault);
	//private LabelExtractorActor labelExtractorActor = new LabelExtractorActor(labelExtractors);
	private final LabelExtractorFactory extractorFactory;
	private final LemonSerializer lemonProxy;
	private final Logger log = Logging.getLogger(this);
	//   private final Repository repository;
	public static final URI reviewstatus = URI.create("http://monnetproject.deri.ie/reviewstatus");
	public static final URI autoreview = URI.create("http://monnetproject.deri.ie/reviewstatus/autoreview");

	/**
	 * Construct a new instance of LeLAManager
	 */
	public LeLAManagerTest(Iterable<GeneratorActor> actors, LabelExtractorFactory extractorFactory) {
		this.actors = new TreeSet<GeneratorActor>(new ActorComparator());
		for (GeneratorActor actor : actors) {
			this.actorPriority.put(actor, actor.getPriority());
			this.actors.add(actor);
		}
		this.extractorFactory = extractorFactory;
		this.lemonProxy = LemonSerializer.newInstance();
	}

	public String getFrag(URI uri) {
		if (uri.getFragment() != null) {
			return uri.getFragment();
		} else if (uri.toString().lastIndexOf("/") > 0) {
			return uri.toString().substring(uri.toString().lastIndexOf("/") + 1);
		} else {
			return uri.toString();
		}
	}

	//@Override
	public LemonModel doGeneration(Ontology ontlg, LemonGeneratorConfig lgc) {
		final LemonSerializer lemonSerializer = LemonSerializer.newInstance();
		final LemonModel model = lemonSerializer.create();
		doGeneration(model, ontlg, lgc);
		return model;
	}

	public Map<Entity, Map<Language, List<LexicalEntry>>> getAllLabels(Ontology ontlg, LemonGeneratorConfig lgc) {		
		final LemonSerializer lemonSerializer = LemonSerializer.newInstance();
		final LemonModel model = lemonSerializer.create();
		Map<Entity, Map<Language, List<LexicalEntry>>> labels = extractLabels(model, ontlg, lgc);
		return labels;
	}


	/**
	 * Perform the generation procedure.
	 */
	//@Override
	public void doGeneration(LemonModel lemonModel, Ontology source, LemonGeneratorConfig config) {
		GenerationReportImpl reportImpl = new GenerationReportImpl();
		try {
			if (config == null) {
				config = new LemonGeneratorConfig();
			}
			log.info("Generating with config " + config);
			LemonElementNamer namer = config.namer(lemonModel);
			Language unlanged = config.unlanged;
			LemonFactory factory = lemonModel.getFactory();
			Map<Language, Lexicon> lexica = new HashMap<Language, Lexicon>();
			Map<Language, GenerationStateImpl> callbackMap = new HashMap<Language, GenerationStateImpl>();
			Collection<Entity> entities = source.getEntities();
			final int entityCount = entities.size();
			int current = 0;
			Set<Language> targetLangs = null;

			if (config.customLabel != null) {
				addCustomLabelURI(config.customLabel);
			}

			if (config.languages != null) {
				targetLangs = config.languages;
			}

			LabelExtractorActorTest lea = new LabelExtractorActorTest(extractorFactory.getExtractor(customURIs,
					config.useDefaultLEP,
					config.inferLang), unlanged, config.lexiconName);

			log.info(entities.size()+" entities to process");
			int cnt = 1;
			for (Entity entity : entities) {
				if (entity.getURI() == null) {
					log.warning("Skipping blank node");
					continue;
				}
				log.info("LeLA generating for " + entity.getURI()+" "+cnt+"/"+entities.size());
				cnt++;
				Map<Language, List<LexicalEntry>> entries;
				entries = lea.perform(entity, lemonModel, namer, targetLangs);
				for (Language language : entries.keySet()) {
					if (targetLangs != null && !targetLangs.contains(language)) {
						continue;
					}
					Lexicon lexicon;
					GenerationStateImpl state;
					if (!lexica.containsKey(language)) {
						lexicon = lemonModel.addLexicon(namer.name(config.lexiconName + "__" + language, null, null), language.toString());
						lexica.put(language, lexicon);
						state = new GenerationStateImpl(lemonModel, entity, language, lemonModel.getFactory(), namer, lexicon, config.lexiconName + "__" + language,  reportImpl, null);
						callbackMap.put(language, state);
						for (LemonGeneratorListener listener : listeners) {
							listener.lexiconAdded(lexicon);
						}
					} else {
						lexicon = lexica.get(language);
						state = new GenerationStateImpl(lemonModel, entity, language, lemonModel.getFactory(), namer, lexicon, config.lexiconName + "__" + language, reportImpl, null);
					}
					for (LexicalEntry entry : entries.get(language)) {
						if(entry == null) continue;
						log.info("  Entry: " + entry);
						lexicon.addEntry(entry);

						state = state.changeEntry(entry);

						boolean hasRef = false;
						for (LexicalSense s2 : entry.getSenses()) {
							if (s2.getReference().equals(entity.getURI())) {
								hasRef = true;
							}
						}

						if (!hasRef) {
							LexicalSense sense = factory.makeSense(namer.name(state.getLexiconName(), state.getEntryName(), "sense"));
							sense.setReference(entity.getURI());
							entry.addSense(sense);
						}

						for (GeneratorActor actor : actors) {
							log.info("Applying actor " + actor.getClass().getName());
							try {
								actor.perform(entry, state);
								log.info("done");
							} catch (Exception x) {
								log.stackTrace(x);
								state.report(new ActorGenerationReportImpl(actor.getClass().getSimpleName(), ActorGenerationReport.Status.EXCEPTION, x.getClass().getName() + ": " + x.getMessage()));
								break;
							}
						}

						while (!state.toProcess.isEmpty()) {
							LexicalEntry entry2 = state.toProcess.pop();
							log.info("entry2 "+entry2.getURI());
							state = state.changeEntry(entry);
							for (GeneratorActor actor : actors) {
								log.info("Applying actor " + actor.getClass().getName());
								try {
									actor.perform(entry2, state);
								} catch (Exception x) {
									log.stackTrace(x);
									state.report(new ActorGenerationReportImpl(actor.getClass().getSimpleName(), ActorGenerationReport.Status.EXCEPTION, x.getClass().getName() + ": " + x.getMessage()));
									break;
								}
							}
						}

						for (LemonGeneratorListener listener : listeners) {
							listener.entryGenerated(entry, (float) current / (float) entityCount);
						}
					}

				}
				current++;
			}
			log.info("Generation complete");
		} catch (Exception x) {
			Logging.stackTrace(log, x);
			throw new RuntimeException(x);
		} finally {
			for (LemonGeneratorListener listener : listeners) {
				listener.onComplete(reportImpl);
			}
		}
	}

	public Map<Entity, Map<Language, List<LexicalEntry>>> extractLabels(LemonModel lemonModel, Ontology source, LemonGeneratorConfig config) {
		//GenerationReportImpl reportImpl = new GenerationReportImpl();
		if (config == null) {
			config = new LemonGeneratorConfig();
		}
		log.info("Generating with config " + config);
		LemonElementNamer namer = config.namer(lemonModel);
		Language unlanged = config.unlanged;
		//LemonFactory factory = lemonModel.getFactory();
		//Map<Language, Lexicon> lexica = new HashMap<Language, Lexicon>();
		List<Map<Language, List<LexicalEntry>>> listEntries = new ArrayList<Map<Language, List<LexicalEntry>>>();
		//Map<Language, GenerationStateImpl> callbackMap = new HashMap<Language, GenerationStateImpl>();
		Collection<Entity> entities = source.getEntities();
		//final int entityCount = entities.size();
		//int current = 0;
		Set<Language> targetLangs = null;

		if (config.customLabel != null) {
			addCustomLabelURI(config.customLabel);
		}

		if (config.languages != null) {
			targetLangs = config.languages;
		}

		LabelExtractorActorTest lea = new LabelExtractorActorTest(extractorFactory.getExtractor(customURIs,
				config.useDefaultLEP,
				config.inferLang), unlanged, config.lexiconName);

		log.info(entities.size()+" entities to process");
		int cnt = 1;
		Map<Entity, Map<Language, List<LexicalEntry>>> entityLabels = new HashMap<Entity, Map<Language, List<LexicalEntry>>>();
		for (Entity entity : entities) {
			if (entity.getURI() == null) {
				log.warning("Skipping blank node");
				continue;
			}
			log.info("LeLA generating for " + entity.getURI()+" "+cnt+"/"+entities.size());
			cnt++;
			Map<Language, List<LexicalEntry>> entries;
			entries = lea.perform(entity, lemonModel, namer, targetLangs);
			entityLabels.put(entity, entries);
			listEntries.add(entries);
		}
	return entityLabels;
	}



	public LemonModel getBlankModel(String baseURI2) {
		String baseURI;
		if (baseURI2.matches(".*#")) {
			baseURI = baseURI2;
		} else {
			baseURI = baseURI2 + "#";
		}
		LemonModel lemonModel = null;
		try {
			Hashtable<String,String> repoProps = new Hashtable<String,String>();
			repoProps.put("sail", "memory");
			Hashtable<String,String> lemonProps = new Hashtable<String,String>();
			// lemonProps.put("repository", repository);
			lemonProps.put("baseURI", baseURI);
			lemonModel = lemonProxy.create();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		return lemonModel;
	}

	private class GenerationStateImpl implements GenerationState {

		private final LemonModel model;
		private final Entity entity;
		private final Language language;
		private final LemonFactory factory;
		private final LemonElementNamer namer;
		private final Lexicon lexicon;
		private final String lexiconName;
		private String entryName;
		private final LinguisticOntology lingOnto = new LexInfo();
		public LinkedList<LexicalEntry> toProcess = new LinkedList<LexicalEntry>();
		private final GenerationReportImpl report;
		private final EntryGenerationReportImpl entryReport;

		/**
		 * Constructor to create new state
		 * 
		 * @param model
		 * @param entity
		 * @param language
		 * @param factory
		 * @param namer
		 * @param lexicon
		 * @param lexiconName
		 * @param report
		 * @param entryReport 
		 */
		public GenerationStateImpl(LemonModel model, Entity entity, Language language, LemonFactory factory, LemonElementNamer namer, Lexicon lexicon, String lexiconName, GenerationReportImpl report, EntryGenerationReportImpl entryReport) {
			this.model = model;
			this.entity = entity;
			this.language = language;
			this.factory = factory;
			this.namer = namer;
			this.lexicon = lexicon;
			this.lexiconName = lexiconName;
			this.entryName = null;
			this.report = report;
			this.entryReport = entryReport;
			if (entryReport != null && entity.getURI() != null) {
				this.report.addEntry(entity.getURI(), entryReport);
			}
		}

		/**
		 * Constructor to copy an existing state 
		 * @param state
		 * @param entryName
		 * @param report 
		 */
		private GenerationStateImpl(GenerationStateImpl state, String entryName, EntryGenerationReportImpl report) {
			this.model = state.model;
			this.entity = state.entity;
			this.language = state.language;
			this.factory = state.factory;
			this.namer = state.namer;
			this.lexicon = state.lexicon;
			this.entryName = entryName;
			this.lexiconName = state.lexiconName;
			this.report = state.report;
			this.entryReport = report;
			this.toProcess.addAll(state.toProcess);
			if (entryReport != null && entity.getURI() != null) {
				this.report.addEntry(entity.getURI(), entryReport);
			}
		}

		public GenerationStateImpl changeEntry(LexicalEntry entry) {
			URI entryURI = entry.getURI();
			if(entryURI == null) {
				log.info("Null path using ID");
				entryURI = URI.create("local:" + entry.getID());
			}
			return new GenerationStateImpl(this, entryURI.getSchemeSpecificPart().substring(entryURI.getSchemeSpecificPart().lastIndexOf("/")+1),
					new EntryGenerationReportImpl(entryURI, new LinkedList<ActorGenerationReport>()));
		}

		//@Override
		public LexicalEntry addLexicalEntry(String form, Language language) {
			try {
				LexicalEntry entry = factory.makeLexicalEntry(namer.name(lexiconName, form, null));
				entry.addAnnotation(reviewstatus, autoreview);
				lexicon.addEntry(entry);
				LexicalForm f = factory.makeForm(namer.name(lexiconName, entryName, "form"));
				entry.addForm(f);
				f.setWrittenRep(new Text(LabelExtractorActorTest.lowerCaseFirst(form), language.toString()));

				toProcess.add(entry);
				return entry;
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}

		//@Override
		public LinguisticOntology getLingOnto() {
			return lingOnto;
		}

		//@Override
		public LemonModel getModel() {
			return model;
		}

		//@Override
		public Entity getEntity() {
			return entity;
		}

		//@Override
		public Language getLanguage() {
			return language;
		}

		//@Override
		public Lexicon getLexicon() {
			return lexicon;
		}

		//@Override
		public LemonElementNamer namer() {
			return namer;
		}

		//@Override
		public String getEntryName() {
			return entryName;
		}

		//@Override
		public String getLexiconName() {
			return lexiconName;
		}

		//@Override
		public void report(ActorGenerationReport report) {
			entryReport.addActorReport(report);
		}
	}
	private final LinkedList<LemonGeneratorListener> listeners = new LinkedList<LemonGeneratorListener>();

	//@Override
	public void addListener(LemonGeneratorListener listener) {
		listeners.add(listener);
	}

	private class ActorComparator  implements Comparator<GeneratorActor> {
		//@Override

		public int compare(GeneratorActor a1, GeneratorActor a2) {
			int rv = actorPriority.get(a1).compareTo(actorPriority.get(a2));
			if (rv == 0) {
				if (a1 == a2) {
					return 0;
				} else if (a1.hashCode() < a2.hashCode()) {
					return -1;
				} else if (a1.hashCode() > a2.hashCode()) {
					return +1;
				} else {
					throw new RuntimeException("Please do not put actors at the same priority, or implement meaningful hash functions");
				}
			} else {
				return rv;
			}
		}

	}
	private HashSet<URI> customURIs = new HashSet<URI>();

	public void addCustomLabelURI(URI uri) {
		customURIs.add(uri);
	}
}