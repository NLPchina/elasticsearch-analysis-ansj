package org.ansj.elasticsearch.rest;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.AnsjRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class RestAnsjAction extends BaseRestHandler {

    public RestAnsjAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(GET, "/_ansj", this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        AnsjRequest ansjRequest = new AnsjRequest();

        ansjRequest.asMap().putAll(request.params());

        return channel -> client.execute(AnsjAction.INSTANCE, ansjRequest, new RestToXContentListener<>(channel));
    }
}
