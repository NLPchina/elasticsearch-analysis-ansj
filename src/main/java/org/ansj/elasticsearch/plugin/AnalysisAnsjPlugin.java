package org.ansj.elasticsearch.plugin;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.TransportAnsjAction;
import org.ansj.elasticsearch.cat.AnalyzerCatAction;
import org.ansj.elasticsearch.cat.AnsjCatAction;
import org.ansj.elasticsearch.index.analysis.AnsjAnalyzerProvider;
import org.ansj.elasticsearch.index.analysis.AnsjTokenizerTokenizerFactory;
import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.elasticsearch.rest.RestAnsjAction;
import org.ansj.lucene9.AnsjAnalyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.client.internal.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.telemetry.TelemetryProvider;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;
import org.elasticsearch.xcontent.NamedXContentRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AnalysisAnsjPlugin extends Plugin implements AnalysisPlugin, ActionPlugin {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool, ResourceWatcherService resourceWatcherService, ScriptService scriptService, NamedXContentRegistry xContentRegistry, Environment environment, NodeEnvironment nodeEnvironment, NamedWriteableRegistry namedWriteableRegistry, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<RepositoriesService> repositoriesServiceSupplier, TelemetryProvider telemetryProvider, AllocationService allocationService, IndicesService indicesService) {
        return Collections.singletonList(new AnsjElasticConfigurator(environment));
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {

        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new HashMap<>();

        for (final AnsjAnalyzer.TYPE type : AnsjAnalyzer.TYPE.values()) {

            extra.put(type.name(), (indexSettings, env, name, settings) -> new AnsjTokenizerTokenizerFactory(indexSettings, name, settings));

            LOG.info("regedit analyzer tokenizer named : {}", type.name());
        }

        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {

        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>();

        for (final AnsjAnalyzer.TYPE type : AnsjAnalyzer.TYPE.values()) {

            extra.put(type.name(), (indexSettings, env, name, settings) -> new AnsjAnalyzerProvider(indexSettings, name, settings));

            LOG.info("regedit analyzer provider named : {}", type.name());
        }

        return extra;
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Collections.singletonList(new ActionHandler<>(AnsjAction.INSTANCE, TransportAnsjAction.class));
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<DiscoveryNodes> nodesInCluster) {
        return Arrays.asList(new RestAnsjAction(), new AnalyzerCatAction(), new AnsjCatAction());
    }
}
