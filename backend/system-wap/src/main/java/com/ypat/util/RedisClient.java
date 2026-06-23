package com.ypat.util;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisClient {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 添加数据对象到缓存
     *
     * @param key      主KEY
     * @param object   数据对象
     * @param timeout  缓存时间：秒
     */
    public void put(String key, Object object, long timeout) {
        putForExpire(key, object, timeout);
    }

    /**
     * 从缓存中获取数据对象
     *
     * @param key      主KEY
     * @return 数据对象
     */
    public Object get(String key) {
        return getForExpire(key);
    }

    /**
     * 从缓存中移除数据对象
     *
     * @param key      主KEY
     */
    public void remove(String key, String childKey) {
        removeForExpire(key);
    }

    /**
     * 添加数据对象到缓存
     *
     * @param key      主KEY
     * @param value    数据对象
     * @param timeout  缓存时间：秒
     */
    public void putForExpire(String key, Object value, long timeout) {
        BoundValueOperations<String, Object> boundValueOperations = redisTemplate.boundValueOps(key);
        boundValueOperations.set(value, timeout, TimeUnit.SECONDS);
    }

    public void putNoExpire(String key, Object value) {
        BoundValueOperations<String, Object> boundValueOperations = redisTemplate.boundValueOps(key);
        boundValueOperations.set(value);
    }

    /**
     * 取通过 putForExpire方法添加的缓存值
     *
     * @param key
     */
    public Object getForExpire(String key) {
        return redisTemplate.boundValueOps(key).get();
    }

    /**
     * 删除通过 putForExpire方法添加的缓存值
     *
     * @param key
     */
    public void removeForExpire(String key) {
        redisTemplate.delete(key);
    }

}