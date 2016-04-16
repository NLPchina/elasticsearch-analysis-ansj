package org.ansj.elasticsearch.index.analysis;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene5.AnsjAnalyzer;
import org.ansj.lucene5.AnsjAnalyzer.TYPE;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettingsService;

public class AnsjAnalyzerProvider extends AbstractIndexAnalyzerProvider<AnsjAnalyzer> {

	private TYPE type;

	@Inject
	public AnsjAnalyzerProvider(Index index, IndexSettingsService indexSettingsService, Environment env, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettingsService.getSettings(), name, settings);

		String typeName = indexSettingsService.getSettings().get("index.analysis.analyzer." + name + ".type");

		if (typeName == null) {
			typeName = settings.get("index.analysis.analyzer." + name + ".type");
		}

		if (typeName == null) {
			AnsjElasticConfigurator.logger
					.error("index.analysis.analyzer." + name + ".type not setting! settings: " + settings.getAsMap() + "  index_settings:" + indexSettingsService.getSettings().getAsMap());
		} else {
			type = TYPE.valueOf(typeName.replace(AnsjAnalysis.SUFFIX, ""));
		}

	}

	@Override
	public AnsjAnalyzer get() {
		return new AnsjAnalyzer(type);
	}

}
