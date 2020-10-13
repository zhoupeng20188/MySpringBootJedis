package com.zp.jedis.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * @Author zhoupeng
 * @Date 2020-05-27 16:47
 */
public class RedisUtil {
    /**
     * 分布式锁-加锁
     *
     * @param lockKey
     * @return
     */
    public static boolean getLock(String lockKey, int expireTime) {
        Jedis jedis = JedisPoolUtil.getInstance().getResource();
        // setnx如果key已存在则不设值，返回0
        // 两条操作不是原子性的，最好用Lua脚本
//        Long setnx = jedis.setnx(lockKey, "1");
//        if (setnx != null && setnx.intValue() == 1) {
//            System.out.println("加锁成功");
//            jedis.expire(lockKey, expireTime);
        // 使用set函数同时设定nx和ex参数，避免使用两个操作
        if(jedis.set(lockKey, "1", SetParams.setParams().nx().ex(expireTime)) != null) {
            return true;
        } else {
            return false;
        }

//        return true;
    }

    /**
     * 释放锁
     * @param lockKey
     * @return
     */
    public static boolean releaLock(String lockKey){
        Jedis jedis = JedisPoolUtil.getInstance().getResource();
        jedis.del(lockKey);
        System.out.println("锁释放成功");
        return true;
    }
}
