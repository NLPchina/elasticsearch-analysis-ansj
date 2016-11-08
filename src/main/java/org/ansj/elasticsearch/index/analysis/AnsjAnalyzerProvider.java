package org.ansj.elasticsearch.index.analysis;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene6.AnsjAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.indices.analysis.AnalysisModule;

import java.util.HashMap;
import java.util.Map;

public class AnsjAnalyzerProvider extends AbstractIndexAnalyzerProvider<AnsjAnalyzer> {

    private final AnsjAnalyzer analyzer;

    @Inject
    public AnsjAnalyzerProvider(IndexSettings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings, AnsjAnalyzer.TYPE type) {
        super(indexSettings, name, settings);

        analyzer = new AnsjAnalyzer(type, AnsjElasticConfigurator.filter);
    }

    @Override
    public AnsjAnalyzer get() {
        return analyzer;
    }

    public static Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>();

        AnsjAnalyzer.TYPE[] values = AnsjAnalyzer.TYPE.values();

        String str;
        for (final AnsjAnalyzer.TYPE type : values) {

            str = type.name() + AnsjElasticConfigurator.SUFFIX;
            extra.put(str, (indexSettings, env, name, settings) -> new AnsjAnalyzerProvider(indexSettings, env, name, settings, type));

            AnsjElasticConfigurator.logger.info("regedit analyzer provider named : {}", str);
        }

        return extra;
    }
}
