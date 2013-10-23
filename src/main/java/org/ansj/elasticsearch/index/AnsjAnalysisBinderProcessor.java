package org.ansj.elasticsearch.index;

import org.ansj.elasticsearch.index.analysis.AnsjIndexAnalyzerProvider;
import org.ansj.elasticsearch.index.analysis.AnsjQueryAnalyzerProvider;
import org.elasticsearch.index.analysis.AnalysisModule;

public class AnsjAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {

    }

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        System.out.println("aaaa");
        analyzersBindings.processAnalyzer("ansj_index", AnsjIndexAnalyzerProvider.class);
        analyzersBindings.processAnalyzer("ansj_query", AnsjQueryAnalyzerProvider.class);
        super.processAnalyzers(analyzersBindings);
    }

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        //      tokenizersBindings.processTokenizer("ansj", AnsjTokenizerFactory.class);
        //      super.processTokenizers(tokenizersBindings);
    }
}
