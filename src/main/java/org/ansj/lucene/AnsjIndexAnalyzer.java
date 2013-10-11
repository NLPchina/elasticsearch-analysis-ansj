package org.ansj.lucene;

import java.io.Reader;
import java.util.Set;

import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;

public class AnsjIndexAnalyzer extends Analyzer {

	public Set<String> filter;
	public boolean pstemming = false;
    
    public AnsjIndexAnalyzer(Settings indexSettings,Settings settings,Set<String> filter,boolean pstemming){
    	super();
    	this.filter = filter;
    	this.pstemming = pstemming;
    }


    @Override
    protected TokenStreamComponents createComponents(String fieldName, final Reader reader) {
        Tokenizer tokenizer = new AnsjTokenizer(new IndexAnalysis(reader), reader, filter, pstemming);
        return new TokenStreamComponents(tokenizer);
    }

}
