package edu.insight.unlp.lemonator;

import eu.monnetproject.label.LabelExtractor;
import eu.monnetproject.label.LanguageInferrer;
import eu.monnetproject.lang.Language;
import eu.monnetproject.ontology.Entity;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class AggregateLabelExtractorTest implements LabelExtractor {
    private final List<LabelExtractor> leps;
    private final LanguageInferrer inferrer;
    private final boolean union;
    
    public AggregateLabelExtractorTest(List<LabelExtractor> leps, boolean union) {
        this.leps = leps;
        this.inferrer = null;
        this.union = union;
    }    
    
    public AggregateLabelExtractorTest(List<LabelExtractor> leps, LanguageInferrer inferrer) {
        this.leps = leps;
        this.inferrer = inferrer;
        this.union = false;
    }

    public AggregateLabelExtractorTest(List<LabelExtractor> leps, LanguageInferrer inferrer, boolean union) {
        this.leps = leps;
        this.inferrer = inferrer;
        this.union = union;
    }
    
    

    
    public Map<Language, Collection<String>> getLabels(Entity entity) {
        Map<Language,Collection<String>> rv = null;
        if(union) {
            rv = new HashMap<Language, Collection<String>>();
        }
        for (LabelExtractor lep : leps) {
            Map<Language, Collection<String>> result = lep.getLabels(entity);
            if (result != null && !result.isEmpty()) {
                if (inferrer != null && result.containsKey(LabelExtractor.NO_LANGUAGE)) {
                    Map<Language, Collection<String>> result2 = new HashMap<Language, Collection<String>>();
                    for (Map.Entry<Language, Collection<String>> e : result.entrySet()) {
                        if (e.getKey().equals(LabelExtractor.NO_LANGUAGE)) {
                            for (String label : e.getValue()) {
                                Language newLang = LabelExtractor.NO_LANGUAGE;//inferrer.getLang(label);                                
                                if (newLang == null) {
                                    newLang = LabelExtractor.NO_LANGUAGE;
                                }
                                if (!result2.containsKey(newLang)) {
                                    result2.put(newLang, new LinkedList<String>());
                                }
                                result2.get(newLang).add(label);
                            }
                        } else {
                            if (!result2.containsKey(e.getKey())) {
                                result2.put(e.getKey(), e.getValue());
                            } else {
                                result2.get(e.getKey()).addAll(e.getValue());
                            }
                        }
                    }
                    if(union) {
                        rv.putAll(result2);
                    } else {
                        return result2;
                    }
                } else {
                    if(union) {
                        rv.putAll(result);
                    } else {
                        return result;
                    }
                }
            }
        }
        if(union) {
            return rv;
        } else {
            return Collections.EMPTY_MAP;
        }
    }
    
}