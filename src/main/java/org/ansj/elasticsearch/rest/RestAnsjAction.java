package org.ansj.elasticsearch.rest;

import static org.elasticsearch.rest.RestRequest.Method.GET;

import java.util.Map;
import java.util.Map.Entry;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.AnsjRequest;
import org.ansj.elasticsearch.action.AnsjResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestToXContentListener;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class RestAnsjAction extends BaseRestHandler {

	@Inject
	public RestAnsjAction(Settings settings, RestController controller, Client client) {
		super(settings, controller, client);
		controller.registerHandler(GET, "/_ansj", this);
	}

	@Override
	public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
		AnsjRequest ansjRequest = new AnsjRequest();

		Map<String, String> params = request.params();

		for (Entry<String, String> entry : params.entrySet()) {
			ansjRequest.put(entry.getKey(), entry.getValue());
		}

		client.execute(AnsjAction.INSTANCE, ansjRequest, new RestToXContentListener<AnsjResponse>(channel));
	}
}