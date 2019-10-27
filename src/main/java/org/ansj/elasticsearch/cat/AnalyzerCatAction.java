package org.ansj.elasticsearch.cat;

import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.CrfLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.StopLibrary;
import org.ansj.library.SynonymsLibrary;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Table;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.RestResponseListener;
import org.elasticsearch.rest.action.cat.AbstractCatAction;
import org.nlpcn.commons.lang.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 分词的cat
 * Created by zhangqinghua on 16/2/2.
 */
public class AnalyzerCatAction extends AbstractCatAction {

    public AnalyzerCatAction(RestController controller) {
        controller.registerHandler(RestRequest.Method.GET, "/_cat/analyze", this);
        controller.registerHandler(RestRequest.Method.GET, "/_cat/{index}/analyze", this);
    }

    @Override
    public String getName() {
        return "ansj_cat_analyzer_action";
    }

    @Override
    protected RestChannelConsumer doCatRequest(RestRequest request, NodeClient client) {
        String[] texts = request.paramAsStringArrayOrEmptyIfAll("text");

        AnalyzeAction.Request analyzeRequest = new AnalyzeAction.Request(request.param("index"));
        analyzeRequest.field(request.param("field"));

        String tokenizer = request.param("tokenizer");
        if (StringUtil.isNotBlank(tokenizer)) {
            analyzeRequest.tokenizer(tokenizer);
        }

        if (texts == null || texts.length == 0) {
            analyzeRequest.text("null");
            analyzeRequest.analyzer("index_ansj");
            return channel -> client.admin().indices().analyze(analyzeRequest, new RestResponseListener<AnalyzeAction.Response>(channel) {
                @Override
                public RestResponse buildResponse(final AnalyzeAction.Response analyzeResponse) throws Exception {
                    return ChineseRestTable.response(channel,
                            "err args example : /_cat/analyze?text=中国&analyzer=index_ansj, other params: [field,tokenizer,token_filters,char_filters]");
                }
            });
        } else {
            analyzeRequest.text(texts);
            analyzeRequest.analyzer(request.param("analyzer"));

            String[] filters = request.paramAsStringArray("token_filters", request.paramAsStringArray("filters", new String[0]));
            for (String filter : filters) {
                analyzeRequest.addTokenFilter(filter);
            }

            filters = request.paramAsStringArray("char_filters", new String[0]);
            for (String filter : filters) {
                analyzeRequest.addCharFilter(filter);
            }

            return channel -> client.admin().indices().analyze(analyzeRequest, new RestResponseListener<AnalyzeAction.Response>(channel) {
                @Override
                public RestResponse buildResponse(final AnalyzeAction.Response analyzeResponse) throws Exception {
                    return ChineseRestTable.buildResponse(buildTable(analyzeResponse, request), channel);
                }
            });
        }
    }

    @Override
    protected void documentation(StringBuilder stringBuilder) {
        stringBuilder.append("/_cat/analyze\n");
    }

    @Override
    protected Table getTableWithHeader(RestRequest restRequest) {
        final Table table = new Table();
        table.startHeaders();
        table.addCell("term", "alias:t;desc:term;text-align:left");
        table.addCell("start_offset", "alias:s;desc:start_offset;text-align:left");
        table.addCell("end_offset", "alias:e;desc:end_offset;text-align:left");
        table.addCell("position", "alias:p;desc:position;text-align:left");
        table.addCell("type", "alias:t;desc:type;text-align:left");
        table.endHeaders();
        return table;
    }

    @Override
    protected Set<String> responseParams() {
        Set<String> responseParams = new HashSet<>(super.responseParams());
        responseParams.addAll(Arrays.asList("text", "index", "field", "analyzer", "tokenizer", "filters", "token_filters", "char_filters", "type", "key",
                "isNameRecognition", "isNumRecognition", "isQuantifierRecognition", "isRealName", "isSkipUserDefine",
                CrfLibrary.DEFAULT, DicLibrary.DEFAULT, AmbiguityLibrary.DEFAULT, StopLibrary.DEFAULT, SynonymsLibrary.DEFAULT));
        return Collections.unmodifiableSet(responseParams);
    }

    private Table buildTable(final AnalyzeAction.Response analyzeResponse, final RestRequest request) {
        Table t = getTableWithHeader(request);
        for (AnalyzeAction.AnalyzeToken token : analyzeResponse.getTokens()) {
            t.startRow();
            t.addCell(token.getTerm());
            t.addCell(token.getStartOffset());
            t.addCell(token.getEndOffset());
            t.addCell(token.getPosition());
            t.addCell(token.getType());
            t.endRow();
        }
        return t;
    }
}
