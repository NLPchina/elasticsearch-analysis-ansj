package org.ansj.elasticsearch.action;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.splitWord.analysis.UserDefineAnalysis;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.action.support.single.shard.TransportSingleShardAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.List;

/**
 *
 * Created by zhangqinghua on 16/2/2.
 */
public class TransportAnsjAction extends TransportSingleShardAction<AnsjRequest,AnsjResponse> {


    @Inject public TransportAnsjAction(Settings settings,
                                  ThreadPool threadPool, ClusterService clusterService,
                                  TransportService transportService, ActionFilters actionFilters,
                                  IndexNameExpressionResolver indexNameExpressionResolver) {

        super(settings, AnsjAction.NAME, threadPool, clusterService,transportService, actionFilters, indexNameExpressionResolver, AnsjRequest.class,ThreadPool.Names.INDEX);
    }

    @Override
    protected AnsjResponse shardOperation(AnsjRequest request, ShardId shardId) {
        String type = request.type();
        String text = request.text();
        List<Term> terms;
        if(type.equals("index")){
            terms = IndexAnalysis.parse(text);
        }else if(type.equals("user")){
            terms = UserDefineAnalysis.parse(text);
        }else{
            terms = ToAnalysis.parse(text);
        }
        return new AnsjResponse(terms);
    }

    @Override
    protected AnsjResponse newResponse() {
        return new AnsjResponse();
    }

    @Override
    protected boolean resolveIndex(AnsjRequest request) {
        return false;
    }

    @Override
    protected ShardsIterator shards(ClusterState clusterState, InternalRequest internalRequest) {
        //just execute local
        return null;
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, InternalRequest request) {
        return null;
    }
}
