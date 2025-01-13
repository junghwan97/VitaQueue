package com.example.productservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RedisService {

    private final RedisTemplate<String, Integer> redisTemplate;

    public RedisService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setValues(String key, Integer value) {
        ValueOperations<String, Integer> values = redisTemplate.opsForValue();
        values.set(key, value);
    }

    public Integer getValue(String key) {
        ValueOperations<String, Integer> values = redisTemplate.opsForValue();
        if (values.get(key) == null) return null;
        return values.get(key);
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
}