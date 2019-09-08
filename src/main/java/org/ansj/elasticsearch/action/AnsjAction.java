package org.ansj.elasticsearch.action;

import org.elasticsearch.action.ActionType;
import org.elasticsearch.common.io.stream.Writeable;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjAction extends ActionType<AnsjResponse> {

    static final String NAME = "cluster:admin/ansj/analyze";

    public static final AnsjAction INSTANCE = new AnsjAction(NAME, AnsjResponse::new);

    public AnsjAction(String name, Writeable.Reader<AnsjResponse> responseReader) {
        super(name, responseReader);
    }
}
