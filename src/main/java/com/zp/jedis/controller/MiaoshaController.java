package com.zp.jedis.controller;

import com.zp.jedis.utils.JedisPoolUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Random;


/**
 * redis实现秒杀
 */
@RestController
public class MiaoshaController {
    private static final String STOCK_KEY = "miaosha:stock";
    private static final String MIAOSHA_SET_NAME = "miaosha:set";
    private static final String MIAOSHA_LUA_SCRIPT = "local userid=KEYS[1];\r\n" +
            "local stockkey ='miaosha:stock';\r\n" +
            "local setname ='miaosha:set';\r\n" +
            "local userExists=redis.call(\"sismember\",setname,userid);\r\n" +
            "if tonumber(userExists)==1 then \r\n" +
            "  return '2';\r\n" +
            "end\r\n" +
            "local num = redis.call(\"get\",stockkey);\r\n" +
            "if tonumber(num)<=0 then \r\n" +
            "  return '0';\r\n" +
            "else \r\n" +
            "  redis.call(\"decr\",stockkey); \r\n" +
            "  redis.call(\"sadd\",setname, userid); \r\n" +
            "end \r\n" +
            "return '1'";


    /**
     * 使用redis事务解决超卖问题
     * 如果库存被其它人修改，则事务不会执行，可以解决超卖问题，但会导致少卖（库存遗留）问题
     * 库存为10，ab -n 1000 -c 200来跑是没问题
     * 库存为200，ab -n 1000 -c 200来跑有库存遗留问题
     * @return
     */
    @RequestMapping("/miaosha")
    public String miaosha(){
        Jedis jedis = JedisPoolUtil.getInstance().getResource();
        // 监视库存，这里必须放到取库存操作之前
        jedis.watch(STOCK_KEY);
        String stock = jedis.get(STOCK_KEY);
        if(stock == null){
            System.out.println("秒杀未开始！");
        } else{
            int stockInt = Integer.parseInt(stock);
            if(stockInt > 0){
                // 库存>0时
                // 判断set中有没有当前用户
                int userId = new Random().nextInt(5000);
                if(jedis.exists(MIAOSHA_SET_NAME) && jedis.sismember(MIAOSHA_SET_NAME, String.valueOf(userId))){
                    System.out.println("您已抢成功，不能重复抢！");
                }else{
                    // 库存-1，将用户加入set中

                    Transaction transaction = jedis.multi();
                    transaction.decr(STOCK_KEY);
                    transaction.sadd(MIAOSHA_SET_NAME, String.valueOf(userId));
                    System.out.println("秒杀成功！");
                    List<Object> exec = transaction.exec();
                    if(exec == null || exec.size() == 0){
                        System.out.println("秒杀失败！");
                    }
                }
            }else{
                System.out.println("秒杀已结束！");
            }
        }
        jedis.close();
        return "OK";
    }

    /**
     * 使用lua脚本解决库存遗留问题
     * lua脚本可以保证原子性
     * @return
     */
    @RequestMapping("/miaosha2")
    public String miaosha2(){
        Jedis jedis = JedisPoolUtil.getInstance().getResource();
        String sha1 = jedis.scriptLoad(MIAOSHA_LUA_SCRIPT);
        int userId = new Random().nextInt(5000);
        String result = (String) jedis.evalsha(sha1, 1, String.valueOf(userId));
        if("0".equals(result)){
            System.out.println("秒杀已结束！");
        } else if("1".equals(result)){
            System.out.println("秒杀成功！");
        } else if("2".equals(result)){
            System.out.println("您已抢成功，不能重复抢！");
        } else{
            System.out.println("秒杀异常");
        }
        jedis.close();
        return "OK";
    }
}
