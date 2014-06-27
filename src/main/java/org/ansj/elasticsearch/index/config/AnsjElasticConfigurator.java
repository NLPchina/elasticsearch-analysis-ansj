package org.ansj.elasticsearch.index.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;


import org.ansj.elasticsearch.pubsub.redis.AddTermRedisPubSub;
import org.ansj.elasticsearch.pubsub.redis.RedisPoolBuilder;
import org.ansj.elasticsearch.pubsub.redis.RedisUtils;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

import org.nlpcn.commons.lang.util.IOUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class AnsjElasticConfigurator {
    public static ESLogger logger = Loggers.getLogger("ansj-analyzer");
    private static boolean loaded = false;
    public static Set<String> filter;
    public static boolean pstemming = false;
    public static Environment environment;
    public static String DEFAULT_USER_LIB_PATH = "ansj/user";
    public static String DEFAULT_AMB_FILE_LIB_PATH = "ansj/ambiguity.dic";
    public static String DEFAULT_STOP_FILE_LIB_PATH = "ansj/stopLibrary.dic";
    public static boolean DEFAULT_IS_NAME_RECOGNITION = true;
    public static boolean DEFAULT_IS_NUM_RECOGNITION = true;
    public static boolean DEFAUT_IS_QUANTIFIE_RRECOGNITION = true;

    public static void init(Settings indexSettings, Settings settings) {
    	if (isLoaded()) {
			return;
		}
    	environment  =new Environment(indexSettings);
        initConfigPath(settings);
        boolean enabledStopFilter = settings.getAsBoolean("enabled_stop_filter", false);
        if(enabledStopFilter) {
            loadFilter(settings);
        }
        try{
        	preheat();
        	logger.info("ansj分词器预热完毕，可以使用!");
        }catch(Exception e){
        	logger.error("ansj分词预热失败，请检查路径");
        }
        initRedis(settings);
        setLoaded(true);
    }
    
    private static void initRedis(final Settings settings) {
		if(null==settings.get("redis.ip")){
			logger.info("没有找到redis相关配置!");
			return;
		}
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
				jedis.subscribe(new AddTermRedisPubSub(), new String[]{channel});
				RedisUtils.closeConnection(jedis);
				
			}
		}).start();
		
	}

    private static void preheat() {
        ToAnalysis.parse("一个词");
    }

    private static void initConfigPath(Settings settings) {
        //是否提取词干
        pstemming = settings.getAsBoolean("pstemming", false);
        //用户自定义辞典
        File path = new File(environment.configFile(),settings.get("user_path",DEFAULT_USER_LIB_PATH));
        MyStaticValue.userLibrary = path.getAbsolutePath();
        logger.debug("用户词典路径:{}",MyStaticValue.userLibrary );
        //用户自定义辞典
        path = new File(environment.configFile(),settings.get("ambiguity",DEFAULT_AMB_FILE_LIB_PATH));
        MyStaticValue.ambiguityLibrary = path.getAbsolutePath();
        logger.debug("歧义词典路径:{}",MyStaticValue.ambiguityLibrary );

        MyStaticValue.isNameRecognition = settings.getAsBoolean("is_name",DEFAULT_IS_NAME_RECOGNITION);

        MyStaticValue.isNumRecognition = settings.getAsBoolean("is_num",DEFAULT_IS_NUM_RECOGNITION);

        MyStaticValue.isQuantifierRecognition = settings.getAsBoolean("is_quantifier",DEFAUT_IS_QUANTIFIE_RRECOGNITION);
        
    }

    private static void loadFilter(Settings settings) {
        Set<String> filters = new HashSet<String>();
        String stopLibraryPath = settings.get("stop_path",DEFAULT_STOP_FILE_LIB_PATH);

        if (stopLibraryPath == null) {
            return;
        }

        File stopLibrary = new File(environment.configFile(), stopLibraryPath);
        logger.debug("停止词典路径:{}",stopLibrary.getAbsolutePath() );
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
        logger.info("ansj停止词典加载完毕!");
    }

    private static void emptyFilter() {
        filter = new HashSet<String>();
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void setLoaded(boolean loaded) {
    	AnsjElasticConfigurator.loaded = loaded;
    }

}
