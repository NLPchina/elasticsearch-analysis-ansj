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

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene6.AnsjAnalyzer;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

import java.util.Map;

public class AnsjTokenizerTokenizerFactory extends AbstractTokenizerFactory {

    private static final Logger LOG = Loggers.getLogger(AnsjTokenizerTokenizerFactory.class);

    @Inject
    public AnsjTokenizerTokenizerFactory(IndexSettings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public Tokenizer create() {
        Settings settings = indexSettings.getSettings().getAsSettings("index.analysis.tokenizer." + name());

        Map<String, String> args = settings.getAsMap();
        if (args.isEmpty()) {
            args = AnsjElasticConfigurator.getDefaults();
            args.put("type", name());
        }

        LOG.debug("instance tokenizer settings : {}", args);

        return AnsjAnalyzer.getTokenizer(null, args);
    }
}
