package org.elasticsearch.index.analysis;

import org.ansj.lucene.AnsjIndexAnalyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

import static org.elasticsearch.index.analysis.AnsjStaticValue.*;

public class AnsjIndexAnalyzerProvider extends AbstractIndexAnalyzerProvider<AnsjIndexAnalyzer> {
    private final AnsjIndexAnalyzer analyzer;
    @Inject
    public AnsjIndexAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        init(indexSettings,settings);
        analyzer=new AnsjIndexAnalyzer(indexSettings,settings,filter,pstemming);
    }

    public AnsjIndexAnalyzerProvider(Index index, Settings indexSettings, String name,
    		Settings settings) {
		super(index, indexSettings, name, settings);
		init(indexSettings,settings);
		analyzer=new AnsjIndexAnalyzer(indexSettings,settings,filter,pstemming);
	}

	public AnsjIndexAnalyzerProvider(Index index, Settings indexSettings,
			String prefixSettings, String name, Settings settings) {
		super(index, indexSettings, prefixSettings, name, settings);
		init(indexSettings,settings);
		analyzer=new AnsjIndexAnalyzer(indexSettings,settings,filter,pstemming);
	}


    @Override public AnsjIndexAnalyzer get() {
        return this.analyzer;
    }
}
