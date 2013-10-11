package org.ansj.lucene;

import java.io.Reader;
import java.util.Set;

import org.ansj.lucene.AnsjTokenizer;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;

public class AnsjQueryAnalyzer extends Analyzer {

    private boolean     pstemming;
    private Set<String> filter;

    
    public AnsjQueryAnalyzer(Settings indexSettings,Settings settings,Set<String> filter,boolean pstemming){
    	super();
    	this.filter = filter;
    	this.pstemming = pstemming;
    }
    

    @Override
    protected TokenStreamComponents createComponents(String fieldName, final Reader reader) {
        Tokenizer tokenizer = new AnsjTokenizer(new ToAnalysis(reader), reader, filter, pstemming);
        return new TokenStreamComponents(tokenizer);
    }

}
