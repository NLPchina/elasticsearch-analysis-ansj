package org.ansj.test;

import org.ansj.elasticsearch.index.analysis.AnsjTokenizerTokenizerFactory;
import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.lucene6.AnsjAnalyzer;
import org.ansj.lucene6.AnsjAnalyzer.TYPE;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ESAnalysisAnsjTests {

    @Test
    public void testDefaultsIcuAnalysis() throws InterruptedException, ExecutionException {
        // System.out.println(analysis);

    }

    @Test
    public void testSettingsFile() {
        Settings settings = Settings.builder()
        		.put("path.home", "")
        		.put("path.conf", "config")
        		.build();
        Environment env = new Environment(settings);
        
        AnsjElasticConfigurator aec = new AnsjElasticConfigurator(env);
        
        AnsjTokenizer tokenizer = (AnsjTokenizer) AnsjAnalyzer.getTokenizer(null, 
        		AnsjTokenizerTokenizerFactory.createArgs(TYPE.index_ansj));
        
        return;
    }
    
    public void main(String... args) {
    	testSettingsFile();
    }

}
