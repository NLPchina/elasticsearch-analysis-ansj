package org.ansj.elasticsearch.pubsub.redis;

import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class RedisPoolBuilder {
	
	public static ESLogger logger = Loggers.getLogger("ansj-redis-pool");
	
	private int maxActive=20;
	private int maxIdle=10;
	private int maxWait=1000;
	private boolean testOnBorrow=true;
	
	private String ipAddress="127.0.0.1:6379";
	private int port=6379;
	private int timeout = 2000;
	private String password;
	
	public int getMaxActive() {
		return maxActive;
	}
	public RedisPoolBuilder setMaxActive(int maxActive) {
		this.maxActive = maxActive;
		return this;
	}
	public int getMaxIdle() {
		return maxIdle;
	}
	public RedisPoolBuilder setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
		return this;
	}
	public int getMaxWait() {
		return maxWait;
	}
	public RedisPoolBuilder setMaxWait(int maxWait) {
		this.maxWait = maxWait;
		return this;
	}
	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}
	public RedisPoolBuilder setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
		return this;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public RedisPoolBuilder setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		return this;
	}
	public int getPort() {
		return port;
	}
	public RedisPoolBuilder setPort(int port) {
		this.port = port;
		return this;
	}
	public int getTimeout() {
		return timeout;
	}
	public RedisPoolBuilder setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}
	public String getPassword() {
		return password;
	}
	public RedisPoolBuilder setPassword(String password) {
		this.password = password;
		return this;
	}

	public JedisPool jedisPool(){
		final JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(getMaxActive());
		config.setMaxIdle(getMaxIdle());
		config.setMaxWait(getMaxWait());
		config.setTestOnBorrow(isTestOnBorrow());
		String[] ipAndPort = getIpAddress().split(":");
		String ip="";
		int port=0;
		if(ipAndPort.length==1){
			ip=ipAndPort[0];
			port = getPort();
		}else{
			ip=ipAndPort[0];
			port = Integer.valueOf(ipAndPort[1]);
		}
		logger.info(ip+":"+port);
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        final String fIp = ip;
        final int fPort = port;
        return AccessController.doPrivileged(new PrivilegedAction<JedisPool>() {
            @Override
            public JedisPool run() {
                return new JedisPool(config, fIp, fPort, getTimeout(), getPassword());
            }
        });
	}
}
