package org.ansj.elasticsearch.action;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.library.*;
import org.ansj.lucene6.AnsjAnalyzer;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.recognition.impl.SynonymsRecgnition;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.*;
import org.ansj.util.MyStaticValue;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.single.shard.TransportSingleShardAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportException;
import org.elasticsearch.transport.TransportResponseHandler;
import org.elasticsearch.transport.TransportService;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.SmartForest;
import org.nlpcn.commons.lang.util.StringUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangqinghua on 16/2/2.
 */
public class TransportAnsjAction extends TransportSingleShardAction<AnsjRequest, AnsjResponse> {

    private static final Logger LOG = Loggers.getLogger(TransportAnsjAction.class);

    private static final String MESSAGE = "flush ok";

    private final AnsjElasticConfigurator cfg;

    @Inject
    public TransportAnsjAction(Settings settings,
                               ThreadPool threadPool, ClusterService clusterService,
                               TransportService transportService, ActionFilters actionFilters,
                               IndexNameExpressionResolver indexNameExpressionResolver,
                               AnsjElasticConfigurator cfg) {
        super(settings, AnsjAction.NAME, threadPool, clusterService, transportService, actionFilters, indexNameExpressionResolver, AnsjRequest::new, ThreadPool.Names.INDEX);

        this.cfg = cfg;
    }

    @Override
    protected AnsjResponse shardOperation(AnsjRequest request, ShardId shardId) {
        String path = request.getPath();
        if ("/_cat/ansj".equals(path)) {
            // 执行分词
            return executeAnalyzer(request);
        } else if ("/_cat/ansj/config".equals(path)) {
            // 显示全部配置
            return showConfig();
        } else if ("/_ansj/flush/config".equals(path)) {
            // 刷新全部配置
            return flushConfigAll();
        } else if ("/_ansj/flush/config/single".equals(path)) {
            // 执行刷新配置
            return flushConfig();
        } else if ("/_ansj/flush/dic".equals(path)) {
            // 更新全部词典
            return flushDicAll(request);
        } else if ("/_ansj/flush/dic/single".equals(path)) {
            // 执行更新词典
            return flushDic(request);
        }

        return new AnsjResponse().put("message", "not find any by path " + path);
    }

    /**
     * 执行分词
     */
    private AnsjResponse executeAnalyzer(AnsjRequest request) {
        AnsjResponse response = new AnsjResponse();

        if (!request.asMap().containsKey("text") || !request.asMap().containsKey("type")) {
            return response.put("message", "err args example: /_cat/ansj?text=中国&type=index_ansj&dic=dic&stop=stop&ambiguity=ambiguity&synonyms=synonyms");
        }

        Analysis analysis;

        String temp;
        String type = request.get("type");

        if (type == null) {
            type = AnsjAnalyzer.TYPE.base_ansj.name();
        }

        switch (AnsjAnalyzer.TYPE.valueOf(type)) {
            case base_ansj:
                analysis = new BaseAnalysis();
                break;
            case index_ansj:
                analysis = new IndexAnalysis();
                break;
            case dic_ansj:
                analysis = new DicAnalysis();
                break;
            case query_ansj:
                analysis = new ToAnalysis();
                break;
            case nlp_ansj:
                analysis = new NlpAnalysis();
                if (StringUtil.isNotBlank(temp = request.get(CrfLibrary.DEFAULT))) {
                    ((NlpAnalysis) analysis).setCrfModel(CrfLibrary.get(temp));
                }
                break;
            default:
                analysis = new BaseAnalysis();
        }

        // 用户自定义词典
        if (StringUtil.isNotBlank(temp = request.get(DicLibrary.DEFAULT))) {
            String[] split = temp.split(",");
            Forest[] forests = new Forest[split.length];
            for (int i = 0; i < forests.length; i++) {
                if (StringUtil.isBlank(split[i])) {
                    continue;
                }
                forests[i] = DicLibrary.get(split[i]);
            }
            analysis.setForests(forests);
        }

        // 歧义词典
        if (StringUtil.isNotBlank(temp = request.get(AmbiguityLibrary.DEFAULT))) {
            analysis.setAmbiguityForest(AmbiguityLibrary.get(temp.trim()));
        }

        // 是否开启人名识别
        if (StringUtil.isNotBlank(temp = request.get("isNameRecognition"))) {
            analysis.setIsNameRecognition(Boolean.valueOf(temp));
        }

        // 是否开启数字识别
        if (StringUtil.isNotBlank(temp = request.get("isNumRecognition"))) {
            analysis.setIsNumRecognition(Boolean.valueOf(temp));
        }

        // 是否开启量词识别
        if (StringUtil.isNotBlank(temp = request.get("isQuantifierRecognition"))) {
            analysis.setIsQuantifierRecognition(Boolean.valueOf(temp));
        }

        // 是否保留原字符
        if (StringUtil.isNotBlank(temp = request.get("isRealName"))) {
            analysis.setIsRealName(Boolean.valueOf(temp));
        }

        Result parse = analysis.parseStr(request.get("text"));

        // 停用词词典
        if (StringUtil.isNotBlank(temp = request.get(StopLibrary.DEFAULT))) {
            String[] split = temp.split(",");
            for (String key : split) {
                StopRecognition stop = StopLibrary.get(key.trim());
                if (stop != null)
                    parse.recognition(stop);
            }
        }

        // 同义词词典
        if (StringUtil.isNotBlank(temp = request.get(SynonymsLibrary.DEFAULT))) {
            String[] split = temp.split(",");
            for (String key : split) {
                SmartForest<List<String>> sf = SynonymsLibrary.get(key.trim());
                if (sf != null)
                    parse.recognition(new SynonymsRecgnition(sf));
            }
        }

        List<Object> list = new ArrayList<>(parse.size());
        for (Term term : parse) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", term.getName());
            map.put("nature", term.getNatureStr());
            map.put("offe", term.getOffe());
            map.put("realName", term.getRealName());
            map.put("synonyms", term.getSynonyms());
            list.add(map);
        }

        response.put("result", list);
        return response;
    }

    /**
     * 显示全部配置
     */
    private AnsjResponse showConfig() {
        Map<String, Object> map = new HashMap<>();
        map.putAll(MyStaticValue.ENV);
        map.put("dic", DicLibrary.keys());
        map.put("stop", StopLibrary.keys());
        map.put("synonyms", SynonymsLibrary.keys());
        map.put("ambiguity", AmbiguityLibrary.keys());
        map.put("crf", CrfLibrary.keys());
        return new AnsjResponse(map);
    }

    /**
     * 刷新全部配置
     */
    private AnsjResponse flushConfigAll() {
        ClusterState clusterState = clusterService.state();
        clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);

        DiscoveryNodes nodes = clusterState.nodes();

        final AnsjRequest req = new AnsjRequest("/_ansj/flush/config/single");

        final AtomicInteger ai = new AtomicInteger(nodes.getSize());

        final Map<String, String> result = new HashMap<>();

        for (final DiscoveryNode node : nodes) {

            result.put(node.getAddress().toString(), "time out");

            TransportResponseHandler<AnsjResponse> rep = new TransportResponseHandler<AnsjResponse>() {
                @Override
                public AnsjResponse newInstance() {
                    return newResponse();
                }

                @Override
                public void handleResponse(AnsjResponse response) {
                    LOG.info("[{}] response: {}", node, response.asMap());
                    ai.decrementAndGet();
                    result.put(node.getAddress().toString(), "success");
                }

                @Override
                public void handleException(TransportException exp) {
                    LOG.warn("failed to send request[path:{},args:{}] to [{}]: {}", req.getPath(), req.asMap(), node, exp);
                    ai.decrementAndGet();
                    result.put(node.getAddress().toString(), "err :" + exp.getMessage());
                }

                @Override
                public String executor() {
                    return ThreadPool.Names.SAME;
                }
            };

            transportService.sendRequest(node, AnsjAction.NAME, req, rep);
        }

        for (int i = 0; i < 20 && ai.get() > 0; ++i) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new AnsjResponse(result);
    }

    /**
     * 执行刷新配置
     */
    private AnsjResponse flushConfig() {
        this.cfg.reloadConfig();
        return showConfig();
    }

    /**
     * 更新全部词典
     */
    private AnsjResponse flushDicAll(AnsjRequest request) {

        ClusterState clusterState = clusterService.state();
        clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);

        DiscoveryNodes nodes = clusterState.nodes();

        final AnsjRequest req = new AnsjRequest("/_ansj/flush/dic/single");

        req.put("key", request.get("key"));

        final AtomicInteger ai = new AtomicInteger(nodes.getSize());

        final Map<String, String> result = new HashMap<>();

        for (final DiscoveryNode node : nodes) {

            result.put(node.getAddress().toString(), "time out");

            transportService.sendRequest(node, AnsjAction.NAME, req, new TransportResponseHandler<AnsjResponse>() {
                @Override
                public AnsjResponse newInstance() {
                    return newResponse();
                }

                @Override
                public void handleResponse(AnsjResponse response) {
                    LOG.info("[{}] response: {}", node, response.asMap());
                    ai.decrementAndGet();
                    result.put(node.getAddress().toString(), "success");
                }

                @Override
                public void handleException(TransportException exp) {
                    LOG.warn("failed to send request[path:{},args:{}] to [{}]: {}", req.getPath(), req.asMap(), node, exp);
                    ai.decrementAndGet();
                    result.put(node.getAddress().toString(), "err :" + exp.getMessage());
                }

                @Override
                public String executor() {
                    return ThreadPool.Names.SAME;
                }
            });
        }

        for (int i = 0; i < 20 && ai.get() > 0; i++) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new AnsjResponse(result);
    }

    /**
     * 执行更新词典
     */
    private AnsjResponse flushDic(AnsjRequest request) {

        Map<String, Object> params = request.asMap();

        LOG.info("to flush {}", params);

        String key = (String) params.get("key");

        try {
            if (key.startsWith(DicLibrary.DEFAULT)) {
                DicLibrary.reload(key);
            } else if (key.startsWith(StopLibrary.DEFAULT)) {
                StopLibrary.reload(key);
            } else if (key.startsWith(SynonymsLibrary.DEFAULT)) {
                SynonymsLibrary.reload(key);
            } else if (key.startsWith(AmbiguityLibrary.DEFAULT)) {
                AmbiguityLibrary.reload(key);
            } else if (key.startsWith(CrfLibrary.DEFAULT)) {
                CrfLibrary.reload(key);
            } else if (key.equals("ansj_config")) {
                this.cfg.reloadConfig();
            } else {
                return new AnsjResponse().put("status", "not find any by " + key);
            }

            LOG.info("flush {} ok", key);
            return new AnsjResponse().put("status", MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("flush {} err: {}", key, e);
            return new AnsjResponse().put("status", e.getMessage());
        }
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
