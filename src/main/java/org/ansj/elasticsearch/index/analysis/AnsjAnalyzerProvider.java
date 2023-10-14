package org.ansj.elasticsearch.index.analysis;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene9.AnsjAnalyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

import java.util.Map;
import java.util.stream.Collectors;

public class AnsjAnalyzerProvider extends AbstractIndexAnalyzerProvider<AnsjAnalyzer> {

    private static final Logger LOG = LogManager.getLogger();

    private final AnsjAnalyzer analyzer;

    @Inject
    public AnsjAnalyzerProvider(IndexSettings indexSettings, String name, Settings settings) {
        super(name, settings);

        Settings settings2 = indexSettings.getSettings().getAsSettings("index.analysis.tokenizer." + name());

        Map<String, String> args = settings2.keySet().stream().collect(Collectors.toMap(k -> k, settings2::get));
        if (args.isEmpty()) {
            args.putAll(AnsjElasticConfigurator.getDefaults());
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
