package org.ansj.elasticsearch.action;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjAction extends Action<AnsjRequest, AnsjResponse, AnsjRequestBuilder> {

    public static final AnsjAction INSTANCE = new AnsjAction();
    static final String NAME = "ansj:analyze";

    private AnsjAction() {
        super(NAME);
    }

    @Override
    public AnsjResponse newResponse() {
        return new AnsjResponse();
    }

    @Override
    public AnsjRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new AnsjRequestBuilder(client, this);
    }
}
