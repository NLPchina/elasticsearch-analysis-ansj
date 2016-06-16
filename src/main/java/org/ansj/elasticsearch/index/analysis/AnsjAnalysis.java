package org.ansj.elasticsearch.index.analysis;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene5.AnsjAnalyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.analysis.PreBuiltTokenizerFactoryFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

/**
 *
 * Created by zhangqinghua on 16/1/29.
 */
public class AnsjAnalysis extends AbstractComponent {

	public static final ESLogger LOG = ESLoggerFactory.getLogger("ansj-module");

	public static final String SUFFIX = "_ansj";

	@Inject
	public AnsjAnalysis(final Settings settings, IndicesAnalysisService indicesAnalysisService, Environment env) {

		super(settings);

		AnsjElasticConfigurator.init(settings, env);

		AnsjAnalyzer.TYPE[] values = AnsjAnalyzer.TYPE.values();

		for (int i = 0; i < values.length; i++) {

			final AnsjAnalyzer.TYPE type = values[i];

			final String name = type.name() + SUFFIX;

			AnsjElasticConfigurator.logger.info("regedit analyzer named : " + name);
			
			
			indicesAnalysisService.analyzerProviderFactories().put(name, new PreBuiltAnalyzerProviderFactory(name, AnalyzerScope.INDICES, new AnsjAnalyzer(type, AnsjElasticConfigurator.filter)));
			
			indicesAnalysisService.tokenizerFactories().put(name, new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
				@Override
				public String name() {
					return name;
				}

				@Override
				public Tokenizer create() {
					LOG.debug("create " + name + " tokenizer");
					return AnsjAnalyzer.getTokenizer(null, type, AnsjElasticConfigurator.filter);
				}
			}));
			
		}

	}

}
