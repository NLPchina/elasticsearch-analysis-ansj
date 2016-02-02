package org.ansj.elasticsearch.index.tokenizer;

import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettingsService;

import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.filter;
import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.init;

/**
 * Created by zhangqinghua on 14-9-3.
 */
public class AnsjQueryTokenizerFactory extends AbstractTokenizerFactory {

    @Inject
    public AnsjQueryTokenizerFactory(Index index, IndexSettingsService indexSettingsService,
                                     @Assisted String name, @Assisted Settings settings,Environment env) {
        super(index, indexSettingsService.getSettings(), name, settings);
        init(settings,env);
    }


    @Override
    public Tokenizer create() {
        return new AnsjTokenizer(new ToAnalysis(), filter);
    }
}