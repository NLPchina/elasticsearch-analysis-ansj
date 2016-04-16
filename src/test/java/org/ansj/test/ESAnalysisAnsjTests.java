package org.ansj.test;

import java.util.concurrent.ExecutionException;

import org.ansj.elasticsearch.index.analysis.AnsjAnalysis;
import org.ansj.elasticsearch.plugin.AnalysisAnsjPlugin;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

public class ESAnalysisAnsjTests  {

	@Test
	public void testDefaultsIcuAnalysis() throws InterruptedException, ExecutionException {
//		final AnalysisService analysisService = createAnalysisService(new Index("test", "_na_"), Settings.EMPTY, new AnalysisAnsjPlugin()::onModule);
//
//		System.out.println(analysis);
		
	}

}
