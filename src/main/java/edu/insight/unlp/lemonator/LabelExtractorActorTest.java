package edu.insight.unlp.lemonator;

import eu.monnetproject.lemon.generator.*;
import eu.monnetproject.lang.Language;
import eu.monnetproject.label.*;
import eu.monnetproject.lemon.*;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.Text;
import eu.monnetproject.ontology.*;
import java.util.*;
import eu.monnetproject.util.Logging;
import eu.monnetproject.util.Logger;

/**
 * Actor that extracts labels
 * @author John McCrae 
 */
public class LabelExtractorActorTest {

    private final LabelExtractor lep;
    private final Language unlanged;
    private final String lexiconPrefix;
    private Logger log = Logging.getLogger(this);

    public LabelExtractorActorTest(LabelExtractor labelExtractor, Language unlanged, String lexiconPrefix) {
        this.lep = labelExtractor;
        this.unlanged = unlanged;
        this.lexiconPrefix = lexiconPrefix;
    }

    @SuppressWarnings("unchecked")
    public Map<Language, List<LexicalEntry>> perform(Entity entity, LemonModel model, LemonElementNamer namer, Set<Language> targetLangs) {
        final LemonFactory factory = model.getFactory();
        Map<Language, List<LexicalEntry>> rval = new HashMap<Language, List<LexicalEntry>>();
        log.config(lep.getClass().getName());
        Map<Language, Collection<String>> labels = lep.getLabels(entity);
        if (labels == null || labels.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        for (Language language : labels.keySet()) {
            if (targetLangs != null && !targetLangs.contains(language) && language != LabelExtractor.NO_LANGUAGE) {
                log.info("Skipping, not in language set");
                continue;
            }
            Language lang2;
            if (language == LabelExtractor.NO_LANGUAGE) {
                if (unlanged != null) {
                    log.warning("No language specified, assuming " + unlanged.getName());
                    lang2 = unlanged;
                } else {
                    log.warning("No language specified, no unlanged");
                    lang2 = Language.ENGLISH;
                }
            } else {
                lang2 = language;
            }
            rval.put(lang2, new LinkedList<LexicalEntry>());
            for (String label : labels.get(language)) {
                log.config("label: " + label);
                LexicalEntry entry = factory.makeLexicalEntry(
                        namer.name(lexiconPrefix + "__" + lang2, label, null));
                entry.addAnnotation(LeLAManagerTest.reviewstatus, LeLAManagerTest.autoreview);
                boolean hasForm = false;
                for (LexicalForm f2 : entry.getForms()) {
                    if (f2.getWrittenRep().value.equals(label)) {
                        hasForm = true;
                    }
                }
                if (!hasForm) {
                    LexicalForm canonicalForm = factory.makeForm(namer.name(lexiconPrefix+ "__"+lang2, label, "form"));
                    canonicalForm.setWrittenRep(new Text(lowerCaseFirst(label), lang2.toString()));
                    entry.addForm(canonicalForm);
                }
                rval.get(lang2).add(entry);
            }
        }
        return rval;
        //}
    }

    public static String lowerCaseFirst(String s) {
        if (s.matches("\\d.*")) {
            return "num_" + s;
        }
        if (s.matches("\\p{Lu}\\p{Ll}+")) {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        } else {
            return s;
        }
    }

    public LemonModel getAuxiliaryLexicon() {
        return null;
    }
}