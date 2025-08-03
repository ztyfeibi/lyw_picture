package com.liyiwei.picturebase.cache;

import java.time.Duration;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.data.cache", value = {"type"}, havingValue = "redis")
public class RedisCacheStrategy extends AbstractCacheTemplate {

    @Autowired
    private StringRedisTemplate redisCache;

    public RedisCacheStrategy() {}

    @Override
    public String get(String key) {
        return redisCache.opsForValue().get(key);
    }

    @Override
    public void put(String key, String value, long seconds) {
        redisCache.opsForValue().set(key, JSONUtil.toJsonStr(value), Duration.ofSeconds(seconds));
    }

}
