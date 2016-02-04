package org.ansj.elasticsearch.index.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


import org.ansj.domain.Term;
import org.ansj.elasticsearch.pubsub.redis.AddTermRedisPubSub;
import org.ansj.elasticsearch.pubsub.redis.RedisPoolBuilder;
import org.ansj.elasticsearch.pubsub.redis.RedisUtils;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.jboss.netty.channel.DefaultFileRegion;
import org.nlpcn.commons.lang.util.IOUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class AnsjElasticConfigurator {
    public static ESLogger logger = Loggers.getLogger("ansj-initializer");
    private static volatile boolean loaded = false;
    public static Set<String> filter;
    public static Environment environment;
    public static String DEFAULT_USER_LIB_PATH = "ansj/dic/user/";
    public static String DEFAULT_REDIS_LIB_PATH = DEFAULT_USER_LIB_PATH+"ext.dic";
    public static File REDIS_LIB_FILE = null;
    public static String DEFAULT_AMB_FILE_LIB_PATH = "ansj/dic/ambiguity.dic";
    public static String DEFAULT_STOP_FILE_LIB_PATH = "ansj/dic/stopLibrary.dic";
    public static boolean DEFAULT_IS_NAME_RECOGNITION = true;
    public static boolean DEFAULT_IS_NUM_RECOGNITION = true;
    public static boolean DEFAUT_IS_QUANTIFIE_RRECOGNITION = false;

    public static void init(Settings settings,Environment env){
        if (isLoaded()) {
            return;
        }
        environment = env;
        Settings ansjSettings = settings.getAsSettings("ansj");
        initConfig(ansjSettings,env);
        boolean enabledStopFilter = ansjSettings.getAsBoolean("enabled_stop_filter", true);
        if(enabledStopFilter) {
            loadFilter(ansjSettings,env);
        }
        try{
            preheat();
            logger.info("ansj分词器预热完毕，可以使用!");
        }catch(Exception e){
            logger.error("ansj分词预热失败，请检查路径");
        }
        initRedis(ansjSettings);
        setLoaded(true);
    }

    private static void initRedis(final Settings settings) {
		if(null==settings.get("redis.ip")){
			logger.info("没有找到redis相关配置!");
			return;
		}
        REDIS_LIB_FILE = environment.configFile().resolve(settings.get("redis.write.dic",DEFAULT_REDIS_LIB_PATH)).toFile();
        new Thread(new  Runnable() {
			@Override
			public void run() {
				RedisPoolBuilder redisPoolBuilder = new RedisPoolBuilder();
				int maxActive = settings.getAsInt("redis.pool.maxactive", redisPoolBuilder.getMaxActive());
				int maxIdle = settings.getAsInt("redis.pool.maxidle", redisPoolBuilder.getMaxIdle());
				int maxWait = settings.getAsInt("redis.pool.maxwait", redisPoolBuilder.getMaxWait());
				boolean testOnBorrow = settings.getAsBoolean("redis.pool.testonborrow", redisPoolBuilder.isTestOnBorrow());
				logger.debug("maxActive:"+maxActive+",maxIdle:"+maxIdle+",maxWait:"+maxWait+",testOnBorrow:"+testOnBorrow );
				
				String ipAndport = settings.get("redis.ip",redisPoolBuilder.getIpAddress());
				int port = settings.getAsInt("redis.port", redisPoolBuilder.getPort());
				String channel = settings.get("redis.channel","ansj_term");
				logger.debug("ip:"+ipAndport+",port:"+port+",channel:"+channel);
				
				JedisPool pool = redisPoolBuilder.setMaxActive(maxActive).setMaxIdle(maxIdle).setMaxWait(maxWait).setTestOnBorrow(testOnBorrow)
				.setIpAddress(ipAndport).setPort(port).jedisPool();
				RedisUtils.setJedisPool(pool);
				final Jedis jedis = RedisUtils.getConnection();
				
				logger.debug("pool:"+(pool==null)+",jedis:"+(jedis==null));
				logger.info("redis守护线程准备完毕,ip:{},port:{},channel:{}",ipAndport,port,channel );
                Objects.requireNonNull(jedis);
                jedis.subscribe(new AddTermRedisPubSub(), channel);
				RedisUtils.closeConnection(jedis);
				
			}
		}).start();
		
	}

    private static void preheat() {
        ToAnalysis.parse("这是一个基于ansj的分词插件");
    }

    private static void initConfig(Settings settings, Environment environment) {

    	Path path = environment.configFile().resolve(settings.get("dic_path",DEFAULT_USER_LIB_PATH));
        MyStaticValue.userLibrary = path.toAbsolutePath().toString();
        logger.debug("用户词典路径:{}",MyStaticValue.userLibrary );

        path = environment.configFile().resolve(settings.get("ambiguity_path",DEFAULT_AMB_FILE_LIB_PATH));
        MyStaticValue.ambiguityLibrary = path.toAbsolutePath().toString();
        logger.debug("歧义词典路径:{}",MyStaticValue.ambiguityLibrary );
        //todo 目前没有使用
//        path = environment.configFile().resolve(settings.get("crf_model_path","ansj/dic/crf.model"));
//        MyStaticValue.crfModel = path.toAbsolutePath().toString();
//        logger.debug("crfModel:{}",MyStaticValue.crfModel );



        MyStaticValue.isRealName = true;

        MyStaticValue.isNameRecognition = settings.getAsBoolean("enable_name_recognition",DEFAULT_IS_NAME_RECOGNITION);

        MyStaticValue.isNumRecognition = settings.getAsBoolean("enable_num_recognition",DEFAULT_IS_NUM_RECOGNITION);

        MyStaticValue.isQuantifierRecognition = settings.getAsBoolean("enable_quantifier_recognition",DEFAUT_IS_QUANTIFIE_RRECOGNITION);
        
        
          
    	
    	//init default用户自定义词典
		File defaultPath =  null;
    	try {
    		String jarPath = java.net.URLDecoder.decode(AnsjElasticConfigurator.class.getProtectionDomain().getCodeSource().getLocation().getFile(),"UTF-8") ;
    		defaultPath = new File(new File(jarPath).getParent(),"default.dic") ;
			UserDefineLibrary.loadFile(UserDefineLibrary.FOREST, defaultPath);
			logger.debug("加载系统内置词典:{}",defaultPath.getAbsolutePath() +" 成功!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("加载系统内置词典:{}",defaultPath +" 失败!");
		}

        
    }
    

    private static void loadFilter(Settings settings, Environment environment) {
        Set<String> filters = new HashSet<>();
        String stopLibraryPath = settings.get("stop_path",DEFAULT_STOP_FILE_LIB_PATH);

        if (stopLibraryPath == null) {
            return;
        }

        File stopLibrary = new File(environment.configFile().toFile(), stopLibraryPath);
        logger.debug("停止词典路径:{}",stopLibrary.getAbsolutePath() );
        if (!stopLibrary.isFile()) {
            logger.info("Can't find the file:" + stopLibraryPath
                        + ", no such file or directory exists!");
            emptyFilter();
            return;
        }

        BufferedReader br;
        try {
            br = IOUtil.getReader(stopLibrary.getAbsolutePath(), "UTF-8");
            String temp;
            while ((temp = br.readLine()) != null) {
                filters.add(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        filter = filters;
        logger.info("ansj停止词典加载完毕!");
    }

    private static void emptyFilter() {
        filter = new HashSet<>();
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void setLoaded(boolean loaded) {
    	AnsjElasticConfigurator.loaded = loaded;
    }

}
