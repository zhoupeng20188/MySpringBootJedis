package com.zp.jedis;

import com.zp.jedis.utils.JedisPoolUtil;
import com.zp.jedis.utils.RedisUtil;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@SpringBootApplication
public class JedisApplication {

    public static void main(String[] args) {
        JedisPool instance = JedisPoolUtil.getInstance();
        JedisPool instance2 = JedisPoolUtil.getInstance();
        System.out.println(instance == instance2);
        Jedis jedis = instance.getResource();
        System.out.println(jedis.ping());
        System.out.println(RedisUtil.getLock("hhh", 3333));
        System.out.println(RedisUtil.getLock("hhh", 3333));
        SpringApplication.run(JedisApplication.class, args);
    }

}
