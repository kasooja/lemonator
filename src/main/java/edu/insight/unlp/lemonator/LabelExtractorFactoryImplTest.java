package edu.insight.unlp.lemonator;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import aQute.bnd.annotation.component.Component;
import eu.monnetproject.label.LabelExtractor;
import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.label.LanguageInferrer;
import eu.monnetproject.label.custom.CustomLabelExtractionPolicy;
import eu.monnetproject.label.rdf.RDFLabelExtractionPolicy;
import eu.monnetproject.label.skos.SKOSLabelExtractionPolicy;
import eu.monnetproject.label.textcat.TextCatInferrer;
import eu.monnetproject.label.uri.URILabelExtractionPolicy;
import eu.monnetproject.label.xbrl.XBRLLabelExtractionPolicy;

@Component(provide = LabelExtractorFactory.class)
public class LabelExtractorFactoryImplTest implements LabelExtractorFactory {
    
    public LabelExtractor getExtractor(Collection<URI> extraURIs, boolean fallback, boolean inferLang) {
        List<LabelExtractor> extractors = new LinkedList<LabelExtractor>();
        if (extraURIs != null) {
            for (URI uri : extraURIs) {
                extractors.add(new CustomLabelExtractionPolicy(uri));
            }
        }
        extractors.add(new RDFLabelExtractionPolicy());
        extractors.add(new SKOSLabelExtractionPolicy());
        extractors.add(new XBRLLabelExtractionPolicy());
        final TextCatInferrer inferrer = inferLang ? new TextCatInferrer() : null;
        if (fallback) {
            return new AggregateLabelExtractorTest(Arrays.asList(new AggregateLabelExtractorTest(extractors, inferrer, true), new URILabelExtractionPolicy()), inferrer);
        } else {
            return new AggregateLabelExtractorTest(extractors, inferrer, true);
        }
    }

    public LabelExtractor getExtractor(List<LabelExtractor> extractors, LanguageInferrer inferrer) {
        return new AggregateLabelExtractorTest(extractors, inferrer);
    }
}