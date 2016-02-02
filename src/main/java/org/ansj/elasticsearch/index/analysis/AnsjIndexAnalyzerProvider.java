package org.ansj.elasticsearch.index.analysis;

import org.ansj.lucene5.AnsjAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettingsService;

import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.filter;
import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.init;

public class AnsjIndexAnalyzerProvider extends AbstractIndexAnalyzerProvider<Analyzer> {
    private final Analyzer analyzer;

    @Inject
    public AnsjIndexAnalyzerProvider(Index index, IndexSettingsService indexSettingsService,
                                     @Assisted String name,
                                     @Assisted Settings settings, Environment env) {
        super(index, indexSettingsService.getSettings(), name, settings);
        init(settings,env);
        analyzer = new AnsjAnalyzer("index", filter);
    }


    @Override
    public Analyzer get() {
        return this.analyzer;
    }
}
