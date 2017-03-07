package org.ansj.elasticsearch.index.analysis;

import java.util.HashMap;
import java.util.Map;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene6.AnsjAnalyzer;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class AnsjAnalyzerProvider extends AbstractIndexAnalyzerProvider<AnsjAnalyzer> {

    private final AnsjAnalyzer analyzer;

    @Inject
    public AnsjAnalyzerProvider(IndexSettings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings, AnsjAnalyzer.TYPE type) {
        super(indexSettings, name, settings);

        //analyzer = new AnsjAnalyzer(type, AnsjElasticConfigurator.filter);
        
        analyzer = new AnsjAnalyzer(AnsjTokenizerTokenizerFactory.createArgs(type));
    }

    @Override
    public AnsjAnalyzer get() {
        return analyzer;
    }
}
