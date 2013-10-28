package org.ansj.elasticsearch.plugin;

import org.ansj.elasticsearch.index.AnsjAnalysisBinderProcessor;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

public class AnalysisAnsjPlugin extends AbstractPlugin {

    @Override public String name() {
        return "analysis-ansj";
    }


    @Override public String description() {
        return "ansj analysis";
    }


    @Override public void processModule(Module module) {
        if (module instanceof AnalysisModule) {
            AnalysisModule analysisModule = (AnalysisModule) module;
            analysisModule.addProcessor(new AnsjAnalysisBinderProcessor());
        }
    }
    
}