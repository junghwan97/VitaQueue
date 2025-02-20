package com.example.productservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setValues(String key, String value) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, value);
    }

    public Integer getValue(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        String value = values.get(key);
        if (value == null) return null;
        try {
            return Integer.parseInt(value); // String → Integer 변환
        } catch (NumberFormatException e) {
            throw new RuntimeException("Redis value error", e);
        }
    }

    public void decrement(String key, Integer value) {
        redisTemplate.opsForValue().decrement(key, value);
    }

    public void increment(String key, Integer value) {
        redisTemplate.opsForValue().increment(key, value);
    }

    public List<String> getKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? List.copyOf(keys) : List.of();
    }

    public void setExpire(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    public void delete(String reservedKey) {
        redisTemplate.delete(reservedKey);
    }

    public Long executeLuaScript(String luaScript, List<String> keys, List<String> args) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        return redisTemplate.execute(redisScript, keys, args.toArray());
    }
}