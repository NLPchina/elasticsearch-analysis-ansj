package org.ansj.elasticsearch.cat;

import java.util.Map;
import java.util.Map.Entry;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.AnsjRequest;
import org.ansj.elasticsearch.action.AnsjResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Table;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.cat.AbstractCatAction;
import org.elasticsearch.rest.action.support.RestResponseListener;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjCatAction extends AbstractCatAction {

	@Inject
	public AnsjCatAction(Settings settings, RestController controller, Client client) {
		super(settings, controller, client);
		controller.registerHandler(RestRequest.Method.GET, "/_cat/ansj", this);
		controller.registerHandler(RestRequest.Method.GET, "/_cat/ansj/config", this);
		controller.registerHandler(RestRequest.Method.GET, "/_cat/ansj/flush", this);
		controller.registerHandler(RestRequest.Method.GET, "/_cat/ansj/flush/single", this);
	}

	@Override
	protected void doRequest(RestRequest request, RestChannel channel, Client client) {
		AnsjRequest ansjRequest = new AnsjRequest(request.path());

		Map<String, String> params = request.params();

		for (Entry<String, String> entry : params.entrySet()) {
			ansjRequest.put(entry.getKey(), entry.getValue());
		}

		client.execute(AnsjAction.INSTANCE, ansjRequest, new RestResponseListener<AnsjResponse>(channel) {
			@Override
			public RestResponse buildResponse(final AnsjResponse ansjResponse) throws Exception {
				return ChineseRestTable.reponse(channel, ansjResponse.asMap());
			}
		});

	}

	@Override
	protected void documentation(StringBuilder sb) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Table getTableWithHeader(RestRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
