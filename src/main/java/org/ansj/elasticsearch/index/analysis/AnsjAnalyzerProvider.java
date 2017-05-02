package org.ansj.elasticsearch.index.analysis;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene6.AnsjAnalyzer;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

import java.util.Map;

public class AnsjAnalyzerProvider extends AbstractIndexAnalyzerProvider<AnsjAnalyzer> {

    private static final Logger LOG = Loggers.getLogger(AnsjAnalyzerProvider.class);

    private final AnsjAnalyzer analyzer;

    @Inject
    public AnsjAnalyzerProvider(IndexSettings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(indexSettings, name, settings);

        Settings settings2 = indexSettings.getSettings().getAsSettings("index.analysis.tokenizer." + name());

        Map<String, String> args = settings2.getAsMap();
        if (args.isEmpty()) {
            args = AnsjElasticConfigurator.getDefaults();
            args.put("type", name());
        }

        LOG.debug("instance analyzer settings : {}", args);

        analyzer = new AnsjAnalyzer(args);
    }

    @Override
    public AnsjAnalyzer get() {
        return analyzer;
    }
}
