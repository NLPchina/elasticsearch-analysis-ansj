package org.ansj.elasticsearch.cat;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.AnsjRequest;
import org.ansj.elasticsearch.action.AnsjResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Table;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.RestResponseListener;
import org.elasticsearch.rest.action.cat.AbstractCatAction;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjCatAction extends AbstractCatAction {

    @Inject
    public AnsjCatAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(RestRequest.Method.GET, "/_cat/ansj", this);
        controller.registerHandler(RestRequest.Method.GET, "/_cat/ansj/config", this);
        controller.registerHandler(RestRequest.Method.GET, "/_ansj/flush/config", this);
        controller.registerHandler(RestRequest.Method.GET, "/_ansj/flush/config/single", this);
        controller.registerHandler(RestRequest.Method.GET, "/_ansj/flush/dic", this);
        controller.registerHandler(RestRequest.Method.GET, "/_ansj/flush/dic/single", this);
    }

    @Override
    protected RestChannelConsumer doCatRequest(RestRequest request, NodeClient client) {
        AnsjRequest ansjRequest = new AnsjRequest(request.path());

        ansjRequest.asMap().putAll(request.params());

        return channel -> client.execute(AnsjAction.INSTANCE, ansjRequest, new RestResponseListener<AnsjResponse>(channel) {
            @Override
            public RestResponse buildResponse(final AnsjResponse ansjResponse) throws Exception {
                return ChineseRestTable.response(channel, ansjResponse.asMap());
            }
        });
    }

    @Override
    protected void documentation(StringBuilder stringBuilder) {

    }

    @Override
    protected Table getTableWithHeader(RestRequest restRequest) {
        return null;
    }
}
