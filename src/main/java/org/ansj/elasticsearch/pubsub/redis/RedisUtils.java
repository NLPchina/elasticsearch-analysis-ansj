package org.ansj.elasticsearch.pubsub.redis;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUtils {
	
	private static JedisPool jedisPool;
	
	public static ESLogger logger = Loggers.getLogger("ansj-redis-utils");
	
	/**       
     * 获取数据库连接        
     * @return conn        
     */       
    public static Jedis getConnection() {  
        Jedis jedis=null;            
        try {                
            jedis=jedisPool.getResource();
        } catch (Exception e) {                
            e.printStackTrace(); 
            logger.error(e.getMessage(), e);
        }            
        return jedis;        
    }     
      
    /**        
     * 关闭数据库连接        
     * @param conn        
     */       
    public static void closeConnection(Jedis jedis) {            
        if (null != jedis) {                
            try {                    
                jedisPool.returnResource(jedis);                
            } catch (Exception e) {  
                e.printStackTrace();   
                logger.error(e.getMessage(), e);
            }            
        }        
    }    
      
    /**        
     * 设置连接池        
     * @param 数据源       
     */       
    public static void setJedisPool(JedisPool JedisPool) {  
    	RedisUtils.jedisPool = JedisPool;        
    }         
      
    /**        
     * 获取连接池        
     * @return 数据源        
     */       
    public static JedisPool getJedisPool() {  
        return jedisPool;        
    }       
	
}
