package org.ansj.elasticsearch.index.analysis;

import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.filter;
import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.init;
import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.pstemming;

import org.ansj.lucene4.AnsjAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

public class AnsjQueryAnalyzerProvider extends AbstractIndexAnalyzerProvider<Analyzer> {
    private final Analyzer analyzer;

    @Inject
    public AnsjQueryAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                     Environment env, @Assisted String name,
                                     @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        init(indexSettings, settings);
        analyzer = new AnsjAnalysis(filter, pstemming);
    }

    public AnsjQueryAnalyzerProvider(Index index, Settings indexSettings, String name,
                                     Settings settings) {
        super(index, indexSettings, name, settings);
        init(indexSettings, settings);
        analyzer = new AnsjAnalysis(filter, pstemming);
    }

    public AnsjQueryAnalyzerProvider(Index index, Settings indexSettings, String prefixSettings,
                                     String name, Settings settings) {
        super(index, indexSettings, prefixSettings, name, settings);
        init(indexSettings, settings);
        analyzer = new AnsjAnalysis(filter, pstemming);
    }

    @Override
    public Analyzer get() {
        return this.analyzer;
    }
}
