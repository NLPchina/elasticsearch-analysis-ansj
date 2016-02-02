package org.ansj.elasticsearch.index.analysis;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.lucene5.AnsjAnalyzer;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.splitWord.analysis.UserDefineAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.analysis.PreBuiltTokenizerFactoryFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.nlpcn.commons.lang.tire.domain.Forest;

/**
 *
 * Created by zhangqinghua on 16/1/29.
 */
public class AnsjAnalysis {

    private final static ESLogger logger = ESLoggerFactory.getLogger("ansj-module");

    @Inject public AnsjAnalysis(final Settings settings,
                                IndicesAnalysisService indicesAnalysisService, Environment env){

        AnsjElasticConfigurator.init(settings,env);

        indicesAnalysisService.analyzerProviderFactories().put("index_ansj",
                new PreBuiltAnalyzerProviderFactory("index_ansj", AnalyzerScope.GLOBAL,new AnsjAnalyzer("index")));

        indicesAnalysisService.analyzerProviderFactories().put("query_ansj",
                new PreBuiltAnalyzerProviderFactory("query_ansj", AnalyzerScope.GLOBAL,new AnsjAnalyzer("query")));
        
        indicesAnalysisService.analyzerProviderFactories().put("user_ansj",
                new PreBuiltAnalyzerProviderFactory("user_ansj", AnalyzerScope.GLOBAL,new AnsjAnalyzer("user")));


        indicesAnalysisService.tokenizerFactories().put("index_ansj",
                new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
                    @Override
                    public String name() {
                        return "index_ansj";
                    }

                    @Override
                    public Tokenizer create() {
                        logger.info("create index_ansj tokenizer");
                        return new AnsjTokenizer(new IndexAnalysis());
                    }
                }));

        indicesAnalysisService.tokenizerFactories().put("query_ansj",
                new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
                    @Override
                    public String name() {
                        return "query_ansj";
                    }

                    @Override
                    public Tokenizer create() {
                        logger.info("create query_ansj tokenizer");
                        return new AnsjTokenizer(new ToAnalysis());
                    }
                }));
        
        
        indicesAnalysisService.tokenizerFactories().put("user_ansj",
                new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
                    @Override
                    public String name() {
                        return "user_ansj";
                    }

                    @Override
                    public Tokenizer create() {
                        logger.info("create user_ansj tokenizer");
                        return new AnsjTokenizer(new UserDefineAnalysis());
                    }
                }));

    }

}
