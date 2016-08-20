package org.ansj.elasticsearch.pubsub.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUtils {

	private static JedisPool jedisPool;

	/**
	 * 获取数据库连接
	 * 
	 * @return conn
	 */
	public static Jedis getConnection() {
		return jedisPool.getResource();
	}

	/**
	 * 关闭数据库连接
	 * 
	 * @param conn
	 */
	public static void closeConnection(Jedis jedis) {
		if (null != jedis) {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 设置连接池
	 * 
	 * @param 数据源
	 */
	public static void setJedisPool(JedisPool JedisPool) {
		RedisUtils.jedisPool = JedisPool;
	}

	/**
	 * 获取连接池
	 * 
	 * @return 数据源
	 */
	public static JedisPool getJedisPool() {
		return jedisPool;
	}

}
