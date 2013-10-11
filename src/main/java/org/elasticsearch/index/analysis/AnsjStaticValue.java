package org.elasticsearch.index.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import love.cq.util.IOUtil;

import org.ansj.redis.AddTermRedisPubSub;
import org.ansj.redis.RedisPoolBuilder;
import org.ansj.redis.RedisUtils;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class AnsjStaticValue {
	public static ESLogger logger = Loggers.getLogger("ansj-analyzer");
	private static boolean loaded = false;
	public static Set<String> filter;
	public static boolean pstemming = false;
	public static Environment environment;

	public static void init(Settings indexSettings,Settings settings) {
		if (isLoaded()) {
			return;
		}
		initConfigPath(indexSettings, settings);
		loadFilter(settings);
		preheat();
		initRedis(settings);
		setLoaded(true);
	}

	private static void preheat() {
		ToAnalysis.parse("一个词");
	}

	private static void initRedis(final Settings settings) {
		if(null==settings.get("redis.ip")){
			logger.info("not found redis configs!");
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
				logger.info("maxActive:"+maxActive+",maxIdle:"+maxIdle+",maxWait:"+maxWait+",testOnBorrow:"+testOnBorrow );
				
				String ipAndport = settings.get("redis.ip",redisPoolBuilder.getIpAddress());
				int port = settings.getAsInt("redis.port", redisPoolBuilder.getPort());
				String channel = settings.get("redis.channel","ansj_term");
				
				logger.info("ip:"+ipAndport+",port:"+port+",channel:"+channel);
				
				JedisPool pool = redisPoolBuilder.setMaxActive(maxActive).setMaxIdle(maxIdle).setMaxWait(maxWait).setTestOnBorrow(testOnBorrow)
				.setIpAddress(ipAndport).setPort(port).jedisPool();
				RedisUtils.setJedisPool(pool);
				final Jedis jedis = RedisUtils.getConnection();
				
				logger.info("pool:"+(pool==null)+",jedis:"+(jedis==null));
				jedis.subscribe(new AddTermRedisPubSub(), new String[]{channel});
				RedisUtils.closeConnection(jedis);
			}
		}).start();
		
	}

	private static void initConfigPath(Settings indexSettings, Settings settings) {
		environment =new Environment(indexSettings);
		// 是否提取词干
		pstemming = settings.getAsBoolean("pstemming", false);
		// 用户自定义词典
		MyStaticValue.userLibrary = settings.get("user_path", MyStaticValue.userLibrary);
		// 歧义词典
		MyStaticValue.ambiguityLibrary = settings.get("ambiguity", MyStaticValue.ambiguityLibrary);
	}
	
	private static void loadFilter(Settings settings){
		Set<String> filters = new HashSet<String>();
		String stopLibraryPath = settings.get("stop_path");
		File stopLibrary = new File(environment.configFile(),stopLibraryPath);
		if (!stopLibrary.isFile()) {
			logger.info("Can't find the file:" + stopLibraryPath + ", no such file or directory exists!");
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
		AnsjStaticValue.loaded = loaded;
	}
}
