package org.ansj.elasticsearch.action;

import org.elasticsearch.action.ActionType;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjAction extends ActionType<AnsjResponse> {

    public static final AnsjAction INSTANCE = new AnsjAction();
    static final String NAME = "cluster:admin/ansj/analyze";

    private AnsjAction() {
        super(NAME, in -> {
            AnsjResponse response = new AnsjResponse();
            response.readFrom(in);
            return response;
        });
    }
}
