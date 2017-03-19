package org.ansj.elasticsearch.action;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjRequestBuilder extends ActionRequestBuilder<AnsjRequest, AnsjResponse, AnsjRequestBuilder> {

    protected AnsjRequestBuilder(ElasticsearchClient client, Action<AnsjRequest, AnsjResponse, AnsjRequestBuilder> action) {
        super(client, action, new AnsjRequest());
    }
}
