/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ansj.elasticsearch.index.analysis;

import org.ansj.lucene5.AnsjAnalyzer;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.analysis.AnalysisModule;

/**
 */
public class AnsjAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

	public static final ESLogger LOG = Loggers.getLogger(AnsjAnalysisBinderProcessor.class);

	@Override
	public void processAnalyzers(AnalyzersBindings analyzersBindings) {

		AnsjAnalyzer.TYPE[] values = AnsjAnalyzer.TYPE.values();

		for (int i = 0; i < values.length; i++) {

			final AnsjAnalyzer.TYPE type = values[i];

			final String name = type.name() ;

			analyzersBindings.processAnalyzer(name, AnsjAnalyzerProvider.class);

			LOG.debug("regedit analyzer provider named : {}", name);
		}

	}

	@Override
	public void processTokenizers(TokenizersBindings tokenizersBindings) {

		AnsjAnalyzer.TYPE[] values = AnsjAnalyzer.TYPE.values();

		for (int i = 0; i < values.length; i++) {

			final AnsjAnalyzer.TYPE type = values[i];

			final String name = type.name() ;

			tokenizersBindings.processTokenizer(name, AnsjTokenizerTokenizerFactory.class);

			LOG.debug("regedit analyzer tokenizer named : {}", name);
		}
	}

}
