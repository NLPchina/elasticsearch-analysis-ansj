package org.ansj.elasticsearch.index.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import love.cq.util.IOUtil;

import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.FailedToResolveConfigException;
import org.elasticsearch.node.internal.InternalSettingsPreparer;

public class AnsjElasticStaticValue {
    public static ESLogger logger = Loggers.getLogger("ansj-analyzer");
    private static boolean loaded = false;
    public static Set<String> filter;
    public static boolean pstemming = false;
    public static Environment environment;

    static {
        init();
    }

    private static void init() {
        Tuple<Settings, Environment> tuple = InternalSettingsPreparer.prepareSettings(
            Builder.EMPTY_SETTINGS, true);
        environment = tuple.v2();
        initConfigPath(tuple.v1());
        loadFilter(tuple.v1());
        preheat();
        setLoaded(true);
    }

    private static void preheat() {
        ToAnalysis.parse("一个词");
    }

    private static void initConfigPath(Settings settings) {
        // 是否提取词干
        pstemming = settings.getAsBoolean("ansj_pstemming", false);
        //用户自定义辞典
        MyStaticValue.userLibrary = getPath(settings.get("ansj_user_path", MyStaticValue.userLibrary));
        //用户自定义辞典
        MyStaticValue.ambiguityLibrary = getPath(settings.get("ansj_ambiguity", MyStaticValue.ambiguityLibrary));
        
    }

    private static String getPath(String path) {
        // TODO Auto-generated method stub
        File file = new File(path) ;
        try {
            if(!file.isFile()){
                URL resolveConfig = environment.resolveConfig(path) ;
                if(path!=null){
                    return resolveConfig.getPath() ;
                }
            }
        } catch (FailedToResolveConfigException e) {
            // TODO Auto-generated catch block
        }
        return path;
    }

    private static void loadFilter(Settings settings) {
        Set<String> filters = new HashSet<String>();
        String stopLibraryPath = settings.get("stop_path");

        if (stopLibraryPath == null) {
            return;
        }

        File stopLibrary = new File(environment.configFile(), stopLibraryPath);
        if (!stopLibrary.isFile()) {
            logger.info("Can't find the file:" + stopLibraryPath
                        + ", no such file or directory exists!");
            emptyFilter();
            setLoaded(true);
            return;
        }

        BufferedReader br;
        try {
            br = IOUtil.getReader(stopLibrary.getAbsolutePath(), "UTF-8");
            String temp = null;
            while ((temp = br.readLine()) != null) {
                filters.add(temp);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        filter = filters;
        logger.info("stop words loaded!");
    }

    private static void emptyFilter() {
        filter = new HashSet<String>();
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void setLoaded(boolean loaded) {
        AnsjElasticStaticValue.loaded = loaded;
    }

    /**
     * 重新加载配置文件
     */
    public void reload() {
        init();
    }
}
