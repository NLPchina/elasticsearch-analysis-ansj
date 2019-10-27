package org.ansj.elasticsearch.action;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjResponse extends ActionResponse implements ToXContentObject {

    private Map<String, Object> map;

    public AnsjResponse() {
        this.map = new HashMap<>();
    }

    public AnsjResponse(Map<String, ?> map) {
        this.map = new HashMap<>();
        this.map.putAll(map);
    }

    public AnsjResponse(StreamInput in) throws IOException {
        super(in);
        map = in.readMap();
    }

    public AnsjResponse put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public AnsjResponse putAll(Map<String, ?> map) {
        this.map.putAll(map);
        return this;
    }

    public Map<String, Object> asMap() {
        return map;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) {
        return builder;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeMap(map);
    }
}
