package org.ansj.elasticsearch.index.config;

import org.ansj.elasticsearch.plugin.AnalysisAnsjPlugin;
import org.ansj.elasticsearch.pubsub.redis.AddTermRedisPubSub;
import org.ansj.elasticsearch.pubsub.redis.RedisPoolBuilder;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.nlpcn.commons.lang.util.IOUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AnsjElasticConfigurator {
    public static final Logger logger = Loggers.getLogger("ansj-initializer");
    private static volatile boolean loaded = false;
    public static Set<String> filter;
    private static final String DEFAULT_USER_LIB_PATH = "ansj/dic/user/";
    private static final String DEFAULT_REDIS_LIB_PATH = DEFAULT_USER_LIB_PATH + "ext.dic";
    public static File REDIS_LIB_FILE = null;
    private static final String DEFAULT_AMB_FILE_LIB_PATH = "ansj/dic/ambiguity.dic";
    public static File AMB_LIB_FILE = null;
    private static final String DEFAULT_STOP_FILE_LIB_PATH = "ansj/dic/stopLibrary.dic";
    private static final boolean DEFAULT_IS_NAME_RECOGNITION = true;
    private static final boolean DEFAULT_IS_NUM_RECOGNITION = true;
    private static final boolean DEFAULT_IS_QUANTIFIER_RECOGNITION = false;

    public static final String SUFFIX = "_ansj";
    private static final String CONFIG_FILE_NAME = "ansj.cfg.yml";

    @Inject
    public AnsjElasticConfigurator(Environment env) {
        if (isLoaded()) {
            return;
        }

        Path configFilePath = env.configFile().resolve(AnalysisAnsjPlugin.PLUGIN_NAME).resolve(CONFIG_FILE_NAME);
        logger.info("try to load ansj config file: {}", configFilePath);
        if (!Files.exists(configFilePath)) {
            configFilePath = Paths.get(new File(AnsjElasticConfigurator.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(), "config").resolve(CONFIG_FILE_NAME);
            logger.info("try to load ansj config file: {}", configFilePath);
        }
        Settings.Builder builder = Settings.builder();
        if (Files.exists(configFilePath)) {
            try {
                builder.loadFromPath(configFilePath);
            } catch (IOException e) {
                logger.error("load ansj config[{}] error: {}", configFilePath, ExceptionsHelper.stackTrace(e));
            }
        } else {
            logger.warn("can't find ansj config file");
        }
        Settings ansjSettings = builder.build().getAsSettings("ansj");
        initConfig(ansjSettings, env);
        boolean enabledStopFilter = ansjSettings.getAsBoolean("enabled_stop_filter", true);
        if (enabledStopFilter) {
            loadFilter(ansjSettings, env);
        }
        preheat();
        logger.info("ansj分词器预热完毕，可以使用!");
        initRedis(ansjSettings, env);
        setLoaded(true);
    }

    private void initRedis(final Settings settings, Environment environment) {
        if (null == settings.get("redis.ip")) {
            logger.info("没有找到redis相关配置!");
            return;
        }

        loadRedisLib(settings, environment);
        new Thread(() -> {
            RedisPoolBuilder redisPoolBuilder = new RedisPoolBuilder();
            int maxActive = settings.getAsInt("redis.pool.maxactive", redisPoolBuilder.getMaxActive());
            int maxIdle = settings.getAsInt("redis.pool.maxidle", redisPoolBuilder.getMaxIdle());
            int maxWait = settings.getAsInt("redis.pool.maxwait", redisPoolBuilder.getMaxWait());
            boolean testOnBorrow = settings.getAsBoolean("redis.pool.testonborrow",
                    redisPoolBuilder.isTestOnBorrow());
            logger.debug("maxActive:{},maxIdle:{},maxWait:{},testOnBorrow:{}", maxActive, maxIdle, maxWait,
                    testOnBorrow);
            String ipAndport = settings.get("redis.ip", redisPoolBuilder.getIpAddress());
            int port = settings.getAsInt("redis.port", redisPoolBuilder.getPort());
            int timeout = settings.getAsInt("redis.timeout", redisPoolBuilder.getTimeout());
            String password = settings.get("redis.password");
            String channel = settings.get("redis.channel", "ansj_term");
            logger.debug("ip:{},port:{},timeout:{},auth:{},channel:{}", ipAndport, port, timeout, password != null, channel);

            JedisPool pool = redisPoolBuilder.setMaxActive(maxActive).setMaxIdle(maxIdle).setMaxWait(maxWait)
                    .setTestOnBorrow(testOnBorrow).setIpAddress(ipAndport).setPort(port).setTimeout(timeout)
                    .setPassword(password).jedisPool();

            logger.info("redis守护线程准备完毕,ip:{},port:{},timeout:{},auth:{},channel:{}", ipAndport, port, timeout, password != null, channel);

            Jedis jedis = null;
            JedisPubSub jedisPubSub = new AddTermRedisPubSub();
            while (true) {
                try {
                    jedis = pool.getResource();
                    jedis.subscribe(jedisPubSub, channel);
                } catch (JedisConnectionException ex) {
                    logger.warn("subscribe to channel[{}] error: {}", channel, ExceptionsHelper.stackTrace(ex));

                    if (null != jedis) {
                        pool.returnBrokenResource(jedis);
                    }

                    try {
                        TimeUnit.SECONDS.sleep(15);
                    } catch (InterruptedException e) {
                        logger.error(ExceptionsHelper.stackTrace(e));
                    }
                }
            }
        }).start();
    }

    private void preheat() {
        ToAnalysis.parse("这是一个基于ansj的分词插件");
    }

    private void initConfig(Settings settings, Environment environment) {

        Path path = environment.configFile().resolve(settings.get("dic_path", DEFAULT_USER_LIB_PATH));
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                MyStaticValue.DIC.put(MyStaticValue.DIC_DEFAULT, path.toAbsolutePath().toString());
                return null;
            }
        });

        logger.debug("用户词典路径:{}", path.toAbsolutePath().toString());

        AMB_LIB_FILE = environment.configFile().resolve(settings.get("ambiguity_path", DEFAULT_AMB_FILE_LIB_PATH)).toFile();
        MyStaticValue.ambiguityLibrary = AMB_LIB_FILE.getAbsolutePath();
        logger.debug("歧义词典路径:{}", MyStaticValue.ambiguityLibrary);
        // todo 目前没有使用
        // path =
        // environment.configFile().resolve(settings.get("crf_model_path","ansj/dic/crf.model"));
        // MyStaticValue.crfModel = path.toAbsolutePath().toString();
        // logger.debug("crfModel:{}",MyStaticValue.crfModel );

        // 是否显示真实词语
        MyStaticValue.isRealName = true;

        // 是否开启人名识别
        MyStaticValue.isNameRecognition = settings.getAsBoolean("enable_name_recognition", DEFAULT_IS_NAME_RECOGNITION);

        // 是否开启数字识别
        MyStaticValue.isNumRecognition = settings.getAsBoolean("enable_num_recognition", DEFAULT_IS_NUM_RECOGNITION);

        // 是否数字和量词合并
        MyStaticValue.isQuantifierRecognition = settings.getAsBoolean("enable_quantifier_recognition",
                DEFAULT_IS_QUANTIFIER_RECOGNITION);

        // 是否用户词典不加载相同的词
        MyStaticValue.isSkipUserDefine = settings.getAsBoolean("enable_skip_user_define", MyStaticValue.isSkipUserDefine);

        // init default用户自定义词典
        String defaultPath = null;
        try {
            String jarPath = java.net.URLDecoder.decode(
                    AnsjElasticConfigurator.class.getProtectionDomain().getCodeSource().getLocation().getFile(),
                    "UTF-8");
            defaultPath = new File(new File(jarPath).getParent(), "default.dic").getAbsolutePath();
            UserDefineLibrary.loadLibrary(UserDefineLibrary.FOREST, defaultPath);
            logger.debug("加载系统内置词典:{} 成功!", defaultPath);
        } catch (UnsupportedEncodingException e) {
            logger.error("加载系统内置词典:{} 失败!", defaultPath);
        }

    }

    private void loadFilter(Settings settings, Environment environment) {
        Set<String> filters = new HashSet<>();
        String stopLibraryPath = settings.get("stop_path", DEFAULT_STOP_FILE_LIB_PATH);
        if (stopLibraryPath == null) {
            return;
        }

        File stopLibrary = new File(environment.configFile().toFile(), stopLibraryPath);
        logger.debug("停止词典路径:{}", stopLibrary.getAbsolutePath());
        if (!stopLibrary.isFile()) {
            logger.info("Can't find the file:{}, no such file or directory exists!", stopLibraryPath);
            emptyFilter();
            return;
        }

        try (BufferedReader br = IOUtil.getReader(stopLibrary.getAbsolutePath(), "UTF-8")) {
            String temp;
            while ((temp = br.readLine()) != null) {
                filters.add(temp);
            }
        } catch (IOException e) {
            logger.info("ansj停用词典加载出错!");
        }
        filter = filters;
        logger.info("ansj停止词典加载完毕!");
    }

    private void loadRedisLib(Settings settings, Environment environment) {
        REDIS_LIB_FILE = environment.configFile().resolve(settings.get("redis.write.dic", DEFAULT_REDIS_LIB_PATH))
                .toFile();
        logger.debug("redis词典路径:{}", REDIS_LIB_FILE.getAbsolutePath());
        if (!REDIS_LIB_FILE.isFile()) {
            logger.info("Can't find the file:{}, no such file exists!", REDIS_LIB_FILE.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(REDIS_LIB_FILE))) {
            String temp;
            while ((temp = br.readLine()) != null) {
                UserDefineLibrary.insertWord(temp, "userDefine", 1000);
            }
        } catch (IOException e) {
            logger.error("加载redis词典:{} 失败!", REDIS_LIB_FILE.getAbsolutePath());
        }
        logger.info("加载redis词典:{} 成功!", REDIS_LIB_FILE.getAbsolutePath());
    }

    private void emptyFilter() {
        filter = new HashSet<>();
    }

    private boolean isLoaded() {
        return loaded;
    }

    private void setLoaded(boolean loaded) {
        AnsjElasticConfigurator.loaded = loaded;
    }

}
