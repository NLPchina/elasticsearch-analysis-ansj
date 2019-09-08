package org.ansj.elasticsearch.action;

import org.elasticsearch.action.Action;
import org.elasticsearch.common.io.stream.Writeable;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjAction extends Action<AnsjResponse> {

    public static final AnsjAction INSTANCE = new AnsjAction();
    static final String NAME = "cluster:admin/ansj/analyze";

    private AnsjAction() {
        super(NAME);
    }

    @Override
    public AnsjResponse newResponse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Writeable.Reader<AnsjResponse> getResponseReader() {
        return AnsjResponse::new;
    }
}
