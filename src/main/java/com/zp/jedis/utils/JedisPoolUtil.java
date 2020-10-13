package com.zp.jedis.utils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Author zhoupeng
 * @Date 2020-05-27 16:48
 */
public class JedisPoolUtil {

    private static final String HOST = "192.168.223.129";
//    private static final String HOST = "192.168.1.11";
    private static final int PORT = 6379;

    private static JedisPool jedisPool;

    private JedisPoolUtil() {
    }

    public static JedisPool getInstance(){
        if(jedisPool == null){
            synchronized (JedisPoolUtil.class){
                if (jedisPool == null) {
                    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
                    jedisPoolConfig.setMaxTotal(200);
                    jedisPoolConfig.setMaxIdle(32);
                    jedisPoolConfig.setMaxWaitMillis(100 * 1000);
                    jedisPoolConfig.setBlockWhenExhausted(true);
                    jedisPoolConfig.setTestOnBorrow(true);
                    jedisPool = new JedisPool(jedisPoolConfig, HOST, PORT, 60000);
                }
            }
        }
        return jedisPool;
    }
}
