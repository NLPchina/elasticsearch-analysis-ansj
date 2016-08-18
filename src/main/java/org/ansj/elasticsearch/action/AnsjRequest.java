package org.ansj.elasticsearch.action;

import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.action.support.single.shard.SingleShardRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class AnsjRequest extends SingleShardRequest<AnsjRequest> {

    private String type;
    private String text;
    private BytesReference source;

    public AnsjRequest(){}

    public String text() {
        return text;
    }

    public AnsjRequest text(String text) {
        this.text = text;
        return this;
    }

    public String type() {
        return type;
    }

    public AnsjRequest type(String type) {
        this.type = type;
        return this;
    }


    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        type = in.readString();
        text = in.readString();
        source = in.readBytesReference();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(type);
        out.writeString(text);
        out.writeBytesReference(source);
    }

    public BytesReference source() {
        return source;
    }

    public AnsjRequest source(QuerySourceBuilder sourceBuilder) {
        this.source = sourceBuilder.buildAsBytes(Requests.CONTENT_TYPE);
        return this;
    }

    public AnsjRequest source(Map<String, ?> querySource) {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(Requests.CONTENT_TYPE);
            builder.map(querySource);
            return source(builder);
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate [" + querySource + "]", e);
        }
    }

    public AnsjRequest source(XContentBuilder builder) {
        this.source = builder.bytes();
        return this;
    }

    public AnsjRequest source(String querySource) {
        this.source = new BytesArray(querySource);
        return this;
    }

    public AnsjRequest source(byte[] querySource) {
        return source(querySource, 0, querySource.length);
    }

    public AnsjRequest source(byte[] querySource, int offset, int length) {
        return source(new BytesArray(querySource, offset, length));
    }

    public AnsjRequest source(BytesReference querySource) {
        this.source = querySource;
        return this;
    }
}
