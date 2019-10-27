package org.ansj.elasticsearch.rest;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.AnsjRequest;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.CrfLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.StopLibrary;
import org.ansj.library.SynonymsLibrary;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class RestAnsjAction extends BaseRestHandler {

    public RestAnsjAction(RestController controller) {
        controller.registerHandler(GET, "/_ansj", this);
        controller.registerHandler(POST, "/_ansj", this);
    }

    @Override
    public String getName() {
        return "ansj_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        AnsjRequest ansjRequest = new AnsjRequest();

        ansjRequest.asMap().putAll(request.params());

        return channel -> client.execute(AnsjAction.INSTANCE, ansjRequest, new RestToXContentListener<>(channel));
    }

    @Override
    protected Set<String> responseParams() {
        Set<String> responseParams = new HashSet<>(super.responseParams());
        responseParams.addAll(Arrays.asList("text", "index", "field", "analyzer", "tokenizer", "filters", "token_filters", "char_filters", "type", "key",
                "isNameRecognition", "isNumRecognition", "isQuantifierRecognition", "isRealName", "isSkipUserDefine",
                CrfLibrary.DEFAULT, DicLibrary.DEFAULT, AmbiguityLibrary.DEFAULT, StopLibrary.DEFAULT, SynonymsLibrary.DEFAULT));
        return Collections.unmodifiableSet(responseParams);
    }
}
