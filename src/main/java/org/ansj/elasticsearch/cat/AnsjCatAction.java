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
    }

    @Override
    protected RestChannelConsumer doCatRequest(RestRequest request, NodeClient client) {
        AnsjRequest ansjRequest = new AnsjRequest();
        ansjRequest.text(request.param("text"));
        if (request.hasParam("type")) {
            ansjRequest.type(request.param("type"));
        } else {
            ansjRequest.type("index");
        }

        return channel -> client.execute(AnsjAction.INSTANCE, ansjRequest, new RestResponseListener<AnsjResponse>(channel) {
            @Override
            public RestResponse buildResponse(final AnsjResponse ansjResponse) throws Exception {
                return ChineseRestTable.buildResponse(buildTable(ansjResponse, request), channel);
            }
        });
    }

    @Override
    protected void documentation(StringBuilder stringBuilder) {
        stringBuilder.append("/_cat/ansj\n");
    }

    @Override
    protected Table getTableWithHeader(RestRequest restRequest) {
        final Table table = new Table();
        table.startHeaders();
        table.addCell("name", "alias:n;desc:name;text-align:left");
        table.addCell("real_name", "alias:rn;desc:real_name;text-align:left");
        table.addCell("nature", "alias:na;desc:nature;text-align:left");
        table.addCell("offset", "alias:o;desc:offset;text-align:left");
        table.endHeaders();
        return table;
    }

    private Table buildTable(final AnsjResponse ansjResponse, final RestRequest request) {
        Table t = getTableWithHeader(request);
        for (AnsjResponse.AnsjTerm term : ansjResponse.getTerms()) {
            t.startRow();
            t.addCell(term.getName());
            t.addCell(term.getRealName());
            t.addCell(term.getNature());
            t.addCell(term.getOffset());
            t.endRow();
        }
        return t;
    }
}
