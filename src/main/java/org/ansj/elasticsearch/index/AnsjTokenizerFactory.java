package org.ansj.elasticsearch.index;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class AnsjTokenizerFactory extends AbstractTokenizerFactory {

	public AnsjTokenizerFactory(Index index, Settings indexSettings,
			String name, Settings settings) {
		super(index, indexSettings, name, settings);
	}

	@Override
	public Tokenizer create(Reader reader) {
		// TODO Auto-generated method stub
		return null;
	}
	  
}
