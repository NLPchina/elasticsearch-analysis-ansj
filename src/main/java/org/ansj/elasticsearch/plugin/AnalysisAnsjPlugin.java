package org.ansj.elasticsearch.plugin;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.TransportAnsjAction;
import org.ansj.elasticsearch.cat.AnalyzerCatAction;
import org.ansj.elasticsearch.cat.AnsjCatAction;
import org.ansj.elasticsearch.index.analysis.AnsjAnalyzerProvider;
import org.ansj.elasticsearch.index.analysis.AnsjTokenizerTokenizerFactory;
import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.elasticsearch.rest.RestAnsjAction;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.inject.multibindings.Multibinder;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.action.cat.AbstractCatAction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnalysisAnsjPlugin extends Plugin implements AnalysisPlugin, ActionPlugin {

    public static final String PLUGIN_NAME = "analysis-ansj";

    public Collection<Module> createGuiceModules() {
        return Collections.singletonList(new AnsjModule());
    }

    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        return AnsjTokenizerTokenizerFactory.getTokenizers();
    }

    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        return AnsjAnalyzerProvider.getAnalyzers();
    }

    public List<ActionHandler<? extends ActionRequest<?>, ? extends ActionResponse>> getActions() {
        return Collections.singletonList(new ActionHandler<>(AnsjAction.INSTANCE, TransportAnsjAction.class));
    }

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