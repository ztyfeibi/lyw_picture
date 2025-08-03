package com.liyiwei.picturebase.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "spring.data.cache", value = {"type"}, havingValue = "local")
public class LocalCacheStrategy extends AbstractCacheTemplate {

    private final Cache<String,String> localCache;

    public LocalCacheStrategy() {
        this.localCache =  Caffeine.newBuilder()
                .initialCapacity(1024)
                .maximumSize(10_000L)
                .expireAfterWrite(5L, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public String get(String key) {
        return localCache.getIfPresent(key);
    }

    @Override
    public void put(String key, String value, long seconds) {
        localCache.put(key,value);
    }
}
