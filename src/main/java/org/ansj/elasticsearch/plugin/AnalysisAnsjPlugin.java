package org.ansj.elasticsearch.plugin;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.TransportAnsjAction;
import org.ansj.elasticsearch.cat.AnalyzerCatAction;
import org.ansj.elasticsearch.cat.AnsjCatAction;
import org.ansj.elasticsearch.index.analysis.AnsjAnalyzerProvider;
import org.ansj.elasticsearch.index.analysis.AnsjTokenizerTokenizerFactory;
import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.elasticsearch.rest.RestAnsjAction;
import org.ansj.lucene6.AnsjAnalyzer;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.inject.multibindings.Multibinder;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.action.cat.AbstractCatAction;

import java.util.*;

public class AnalysisAnsjPlugin extends Plugin implements AnalysisPlugin, ActionPlugin {

    private static final Logger LOG = Loggers.getLogger(AnalysisAnsjPlugin.class);

    public static final String PLUGIN_NAME = "analysis-ansj";

    @Override
    public Collection<Module> createGuiceModules() {
        return Collections.singletonList(new AnsjModule());
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
    public List<Class<? extends RestHandler>> getRestHandlers() {
        return Collections.singletonList(RestAnsjAction.class);
    }

    private class AnsjModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(AnsjElasticConfigurator.class).asEagerSingleton();
            Multibinder<AbstractCatAction> catActionMultibinder = Multibinder.newSetBinder(binder(), AbstractCatAction.class);
            catActionMultibinder.addBinding().to(AnalyzerCatAction.class).asEagerSingleton();
            catActionMultibinder.addBinding().to(AnsjCatAction.class).asEagerSingleton();
        }
    }
}
