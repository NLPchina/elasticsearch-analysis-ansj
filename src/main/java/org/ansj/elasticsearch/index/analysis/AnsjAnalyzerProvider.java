package org.ansj.elasticsearch.index.analysis;

import org.ansj.lucene5.AnsjAnalyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettingsService;

public class AnsjAnalyzerProvider extends AbstractIndexAnalyzerProvider<AnsjAnalyzer> {

	public static final ESLogger LOG = Loggers.getLogger(AnsjAnalyzerProvider.class);

	@Inject
	public AnsjAnalyzerProvider(Index index, IndexSettingsService indexSettingsService, Environment env, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettingsService.getSettings(), name, settings);
	}

	@Override
	public AnsjAnalyzer get() {
		Settings settings = indexSettings.getAsSettings("index.analysis.tokenizer." + name());
		if (LOG.isDebugEnabled()) {
			LOG.debug("instance analyzer settings : {}", settings.getAsMap());
		}
		return new AnsjAnalyzer(settings.getAsMap());
	}

}
