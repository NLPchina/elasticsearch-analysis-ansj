package org.ansj.elasticsearch.plugin;

import java.util.Collection;
import java.util.Collections;

import org.ansj.elasticsearch.action.AnsjAction;
import org.ansj.elasticsearch.action.TransportAnsjAction;
import org.ansj.elasticsearch.cat.AnalyzerCatAction;
import org.ansj.elasticsearch.cat.AnsjCatAction;
import org.ansj.elasticsearch.index.analysis.AnsjAnalysis;
import org.ansj.elasticsearch.index.analysis.AnsjAnalysisBinderProcessor;
import org.ansj.elasticsearch.rest.RestAnsjAction;
import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.inject.multibindings.Multibinder;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.rest.action.cat.AbstractCatAction;

public class AnalysisAnsjPlugin extends Plugin {

	@Override
	public String name() {
		return "analysis-ansj";
	}

	@Override
	public String description() {
		return "ansj analysis";
	}

	@Override
	public Collection<Module> nodeModules() {
		return Collections.<Module> singletonList(new AnsjModule());
	}

	public void onModule(ActionModule actionModule) {
		actionModule.registerAction(AnsjAction.INSTANCE, TransportAnsjAction.class);
	}

	public void onModule(AnalysisModule model) {
		model.addProcessor(new AnsjAnalysisBinderProcessor());
	}

	public void onModule(RestModule restModule) {
		restModule.addRestAction(RestAnsjAction.class);
	}

	public static class AnsjModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(AnsjAnalysis.class).asEagerSingleton();
			Multibinder<AbstractCatAction> catActionMultibinder = Multibinder.newSetBinder(binder(), AbstractCatAction.class);
			catActionMultibinder.addBinding().to(AnalyzerCatAction.class).asEagerSingleton();
			catActionMultibinder.addBinding().to(AnsjCatAction.class).asEagerSingleton();
		}
	}

}