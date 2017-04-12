package org.ansj.elasticsearch.index.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;

import org.ansj.dic.PathToStream;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.StopLibrary;
import org.ansj.library.SynonymsLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;

public class AnsjElasticConfigurator {

	public static final ESLogger LOG = Loggers.getLogger(AnsjElasticConfigurator.class);

	private static volatile boolean loaded = false;

	private static String path = null;

	private static Settings ansjSettings;

	private static File configDir;

	public synchronized static void init(Settings settings, Environment env) {

		if (isLoaded()) {
			return;
		}

		ansjSettings = settings.getAsSettings("ansj");

		path = settings.get("ansj_config");

		configDir = env.configFile().toFile();

		flushConfig();

		//进行一次测试分词
		preheat();
		LOG.info("init ansj plugin ok , goodluck youyou");

		setLoaded(true);
	}

	private static void flushConfig() {

		MyStaticValue.ENV.clear();

		if (ansjSettings != null) {
            initConfig(ansjSettings);
        }

        //设置全局变量
        setGlobalVar(MyStaticValue.ENV);

		if (path != null) {
			initConfig(path, true);
		} else {
			initConfig(new File(configDir, "ansj_library.properties").getAbsolutePath(), false);
		}
	}

	private static void initConfig(String path, boolean printErr) {
		try (InputStream stream = PathToStream.stream(path)) {
			BufferedReader br = IOUtil.getReader(stream, "utf-8");
			String temp = null;

			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp) || temp.trim().charAt(0) == '#' || !temp.contains("=")) {
					continue;
				}

				int index = temp.indexOf('=');

				MyStaticValue.ENV.put(temp.substring(0, index).trim(), temp.substring(index + 1, temp.length()).trim());
			}

		} catch (Exception e) {
			if (printErr) {
				LOG.error(path + " load err", e);
			} else {
				LOG.warn(path + " load err");
			}
		}
	}

	private static void preheat() {
		ToAnalysis.parse("这是一个基于ansj的分词插件");
	}

	private synchronized static void initConfig(Settings settings) {
		//插入到变量中
		MyStaticValue.ENV.putAll(settings.getAsMap());
	}

	/**
	 * 设置一些全局变量
	 * 
	 * @param map
	 */
	private static void setGlobalVar(Map<String, String> map) {

		// 是否开启人名识别
		if (map.containsKey("isNameRecognition"))
			MyStaticValue.isNameRecognition = Boolean.valueOf(map.get("isNameRecognition"));

		// 是否开启数字识别
		if (map.containsKey("isNumRecognition"))
			MyStaticValue.isNumRecognition = Boolean.valueOf(map.get("isNumRecognition"));

		// 是否数字和量词合并
		if (map.containsKey("isQuantifierRecognition"))
			MyStaticValue.isQuantifierRecognition = Boolean.valueOf(map.get("isQuantifierRecognition"));

		// 是否用户词典不加载相同的词
		if (map.containsKey("isRealName"))
			MyStaticValue.isRealName = Boolean.valueOf(map.get("isRealName"));
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		AnsjElasticConfigurator.loaded = loaded;
	}

	public static void reloadConfig() {
		flushConfig();
		for (String key : new HashSet<>(DicLibrary.keys())) {
			if (!MyStaticValue.ENV.containsKey(key)) {
				LOG.info("remove key {}" , key);
				DicLibrary.remove(key);
			}
		}

		for (String key : new HashSet<>(StopLibrary.keys())) {
			if (!MyStaticValue.ENV.containsKey(key)) {
				LOG.info("remove key {}" , key);
				StopLibrary.remove(key);
			}
		}

		for (String key : new HashSet<>(SynonymsLibrary.keys())) {
			if (!MyStaticValue.ENV.containsKey(key)) {
				LOG.info("remove key {}" , key);
				SynonymsLibrary.remove(key);
			}
		}

		for (String key : new HashSet<>(AmbiguityLibrary.keys())) {
			if (!MyStaticValue.ENV.containsKey(key)) {
				LOG.info("remove key {}" , key);
				AmbiguityLibrary.remove(key);
			}
		}

	}

}
