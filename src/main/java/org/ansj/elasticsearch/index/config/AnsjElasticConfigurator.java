package org.ansj.elasticsearch.index.config;

import org.ansj.dic.PathToStream;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.CrfLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.StopLibrary;
import org.ansj.library.SynonymsLibrary;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.nlpcn.commons.lang.tire.domain.SmartForest;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnsjElasticConfigurator {

    private static final Logger LOG = LogManager.getLogger("ansj-initializer");

    private static final String CONFIG_FILE_NAME = "ansj.cfg.yml";

    private String path;

    private Settings ansjSettings;

    private File configDir;

    private final Environment env;

    @Inject
    public AnsjElasticConfigurator(Environment env) {
        this.env = env;

        //
        init();

        // 进行一次测试分词
        preheat();

        LOG.info("init ansj plugin ok , goodluck youyou");
    }

    private void init() {
        Path configFilePath = env.configFile().resolve("elasticsearch-analysis-ansj").resolve(CONFIG_FILE_NAME);
        LOG.info("try to load ansj config file: {}", configFilePath);
        if (!Files.exists(configFilePath)) {
            configFilePath = Paths.get(new File(AnsjElasticConfigurator.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(), "config").resolve(CONFIG_FILE_NAME);
            LOG.info("try to load ansj config file: {}", configFilePath);
        }
        Settings.Builder builder = Settings.builder();
        if (Files.exists(configFilePath)) {
            try {
                builder.loadFromPath(configFilePath);
                LOG.info("load ansj config: {}", configFilePath);
            } catch (IOException e) {
                LOG.error("load ansj config[{}] error: {}", configFilePath, e);
            }
        } else {
            LOG.warn("can't find ansj config file");
        }

        Settings settings = builder.build();
        path = settings.get("ansj_config");
        ansjSettings = settings.getAsSettings("ansj");
        configDir = env.configFile().toFile();

        flushConfig();
    }

    private void flushConfig() {
        MyStaticValue.ENV.clear();

        // ansj.cfg.yml文件，插入到变量中
        if (ansjSettings != null && !ansjSettings.isEmpty()) {
            MyStaticValue.ENV.putAll(ansjSettings.keySet().stream().collect(Collectors.toMap(k -> k, ansjSettings::get)));
        }

        // ansj.cfg.yml文件中ansj_config指定的文件或者配置文件目录下的ansj_library.properties
        if (path != null) {
            initConfig(path, true);
        } else {
            initConfig(new File(configDir, "ansj_library.properties").getAbsolutePath(), false);
        }

        // 设置全局变量
        setGlobalVar(MyStaticValue.ENV);

        // 加载词典
        for (String k : MyStaticValue.ENV.keySet().toArray(new String[0])) {
            reloadLibrary(k);
        }
    }

    /**
     * 读取配置文件并将配置放入MyStaticValue.ENV
     *
     * @param path
     * @param printErr
     */
    private void initConfig(String path, boolean printErr) {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try (BufferedReader br = IOUtil.getReader(PathToStream.stream(path), "utf-8")) {
                String temp;
                int index;
                while ((temp = br.readLine()) != null) {
                    if (StringUtil.isBlank(temp) || temp.trim().charAt(0) == '#' || !temp.contains("=")) {
                        continue;
                    }

                    index = temp.indexOf('=');

                    MyStaticValue.ENV.put(temp.substring(0, index).trim(), temp.substring(index + 1).trim());
                }
            } catch (Exception e) {
                if (printErr) {
                    LOG.error("{} load err: {}", path, e);
                } else {
                    LOG.warn("{} load err", path);
                }
            }
            return null;
        });
    }

    private void preheat() {
        ToAnalysis.parse("这是一个基于ansj的分词插件");
    }

    /**
     * 设置一些全局变量
     *
     * @param map
     */
    private void setGlobalVar(Map<String, String> map) {
        // 是否开启人名识别
        if (map.containsKey("isNameRecognition")) {
            MyStaticValue.isNameRecognition = Boolean.valueOf(map.get("isNameRecognition"));
        }

        // 是否开启数字识别
        if (map.containsKey("isNumRecognition")) {
            MyStaticValue.isNumRecognition = Boolean.valueOf(map.get("isNumRecognition"));
        }

        // 是否数字和量词合并
        if (map.containsKey("isQuantifierRecognition")) {
            MyStaticValue.isQuantifierRecognition = Boolean.valueOf(map.get("isQuantifierRecognition"));
        }

        // 是否显示真实词语
        if (map.containsKey("isRealName")) {
            MyStaticValue.isRealName = Boolean.valueOf(map.get("isRealName"));
        }

        // 是否用户词典不加载相同的词
        if (map.containsKey("isSkipUserDefine")) {
            MyStaticValue.isSkipUserDefine = Boolean.parseBoolean(map.get("isSkipUserDefine"));
        }
    }

    /**
     * 重新加载配置
     * 如果词典正在使用，重新加载
     * 如果删除了正在使用的词典，清空
     */
    public void reloadConfig() {
        init();
        LOG.info("reload ansj plugin config successfully");

        for (String key : DicLibrary.keys()) {
            if (!MyStaticValue.ENV.containsKey(key)) {
                Optional.ofNullable(DicLibrary.get(key)).ifPresent(SmartForest::clear);

                LOG.info("clear DicLibrary: {}", key);
            }
        }

        for (String key : StopLibrary.keys()) {
            if (!MyStaticValue.ENV.containsKey(key)) {
                Optional.ofNullable(StopLibrary.get(key)).ifPresent(StopRecognition::clear);

                LOG.info("clear StopLibrary: {}", key);
            }
        }

        for (String key : SynonymsLibrary.keys()) {
            if (!MyStaticValue.ENV.containsKey(key)) {
                Optional.ofNullable(SynonymsLibrary.get(key)).ifPresent(SmartForest::clear);

                LOG.info("clear SynonymsLibrary: {}", key);
            }
        }

        for (String key : AmbiguityLibrary.keys()) {
            if (!MyStaticValue.ENV.containsKey(key)) {
                Optional.ofNullable(AmbiguityLibrary.get(key)).ifPresent(SmartForest::clear);

                LOG.info("clear AmbiguityLibrary: {}", key);
            }
        }
    }

    /**
     * 重新加载词典，CRF词典有待处理
     * 如果是正在使用的词典，重新加载
     * 如果是已删除的并且还在使用的，清空
     *
     * @param key
     */
    public void reloadLibrary(String key) {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            if (key.startsWith(DicLibrary.DEFAULT)) {
                if (MyStaticValue.ENV.containsKey(key)) {
                    DicLibrary.reload(key);
                    LOG.info("reload DicLibrary: {}", key);
                } else if (DicLibrary.keys().contains(key)) {
                    Optional.ofNullable(DicLibrary.get(key)).ifPresent(SmartForest::clear);
                    LOG.info("clear DicLibrary: {}", key);
                } else {
                    LOG.warn("DicLibrary[{}] not found", key);
                }
            } else if (key.startsWith(StopLibrary.DEFAULT)) {
                if (MyStaticValue.ENV.containsKey(key)) {
                    StopLibrary.reload(key);
                    LOG.info("reload StopLibrary: {}", key);
                } else if (StopLibrary.keys().contains(key)) {
                    Optional.ofNullable(StopLibrary.get(key)).ifPresent(StopRecognition::clear);
                    LOG.info("clear StopLibrary: {}", key);
                } else {
                    LOG.warn("StopLibrary[{}] not found", key);
                }
            } else if (key.startsWith(SynonymsLibrary.DEFAULT)) {
                if (MyStaticValue.ENV.containsKey(key)) {
                    SynonymsLibrary.reload(key);
                    LOG.info("reload SynonymsLibrary: {}", key);
                } else if (SynonymsLibrary.keys().contains(key)) {
                    Optional.ofNullable(SynonymsLibrary.get(key)).ifPresent(SmartForest::clear);
                    LOG.info("clear SynonymsLibrary: {}", key);
                } else {
                    LOG.warn("SynonymsLibrary[{}] not found", key);
                }
            } else if (key.startsWith(AmbiguityLibrary.DEFAULT)) {
                if (MyStaticValue.ENV.containsKey(key)) {
                    AmbiguityLibrary.reload(key);
                    LOG.info("reload AmbiguityLibrary: {}", key);
                } else if (AmbiguityLibrary.keys().contains(key)) {
                    Optional.ofNullable(AmbiguityLibrary.get(key)).ifPresent(SmartForest::clear);
                    LOG.info("clear AmbiguityLibrary: {}", key);
                } else {
                    LOG.warn("AmbiguityLibrary[{}] not found", key);
                }
            } else if (key.startsWith(CrfLibrary.DEFAULT)) {
                CrfLibrary.reload(key);
                LOG.info("reload CrfLibrary: {}", key);
            }
            return null;
        });
    }

    /**
     * 默认配置
     */
    public static Map<String, String> getDefaults() {
        return MapBuilder.<String, String>newMapBuilder()
                // 是否开启人名识别
                .put("isNameRecognition", MyStaticValue.isNameRecognition.toString())
                // 是否开启数字识别
                .put("isNumRecognition", MyStaticValue.isNumRecognition.toString())
                // 是否数字和量词合并
                .put("isQuantifierRecognition", MyStaticValue.isQuantifierRecognition.toString())
                // 是否显示真实词语
                .put("isRealName", MyStaticValue.isRealName.toString())
                // 是否用户词典不加载相同的词
                .put("isSkipUserDefine", String.valueOf(MyStaticValue.isSkipUserDefine))
                .put(CrfLibrary.DEFAULT, CrfLibrary.DEFAULT)
                .put(DicLibrary.DEFAULT, DicLibrary.DEFAULT)
                .put(StopLibrary.DEFAULT, StopLibrary.DEFAULT)
                .put(SynonymsLibrary.DEFAULT, SynonymsLibrary.DEFAULT)
                .put(AmbiguityLibrary.DEFAULT, AmbiguityLibrary.DEFAULT)
                .immutableMap();
    }
}
