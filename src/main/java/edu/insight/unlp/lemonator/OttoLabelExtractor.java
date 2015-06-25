package edu.insight.unlp.lemonator;

import java.util.List;
import java.util.Map;

import eu.monnetproject.data.FileDataSource;
import eu.monnetproject.framework.services.Services;
import eu.monnetproject.lang.Language;
import eu.monnetproject.lemon.generator.GeneratorActor;
import eu.monnetproject.lemon.generator.LemonGeneratorConfig;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.ontology.OntologySerializer;

public class OttoLabelExtractor {

	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			System.err.println("Usage: java -jar  OttoLabelExtractor.jar ontology.owl labels.tsv");
			return;
		}
		OntologySerializer serializer = Services.get(OntologySerializer.class);
		//LemonSerializer lemonSerializer = Services.get(LemonSerializer.class);
		@SuppressWarnings("deprecation")
		Ontology ontology = serializer.read(new FileDataSource(args[0]));
		LabelExtractorFactoryImplTest labelExtractorFac = new LabelExtractorFactoryImplTest();

		LeLAManagerTest lelaManager = new LeLAManagerTest(Services.getAll(GeneratorActor.class), labelExtractorFac);
		//		final LeLAManager lelaManager = new LeLAManager(Services.getAll(GeneratorActor.class),
		//				Services.get(LabelExtractorFactory.class));
		//
		//	
		StringBuilder b = new StringBuilder();
		Map<Entity, Map<Language, List<LexicalEntry>>> labels = lelaManager.getAllLabels(ontology, new LemonGeneratorConfig());
		for(Entity entity : labels.keySet()){
			Map<Language, List<LexicalEntry>> entries = labels.get(entity);						
			for(Language lan : entries.keySet()){
				String langCode = lan.getIso639_1();								
				List<LexicalEntry> list = entries.get(lan);
				for(LexicalEntry entry : list){
					String label = entry.toString().replaceAll("unknown:/lexicon.*" + langCode + "/", "").trim();
					b.append(entity.getURI().toString() + "\t" + langCode + "\t" + label + "\n");					
				}		
			}	
		}
		BasicFileTools.writeFile(args[1], b.toString().trim());
	}



}
