package com.liyiwei.picturebase.cache;

import java.util.function.Supplier;

public abstract class AbstractCacheTemplate{

//    /**
//     * 加载缓存模板
//     * @param key
//     * @param supplier
//     * @param seconds
//     * @return
//     */
//    public T load(String key, Supplier<T> supplier,long seconds) {
//        T value = (T) get(key);
//        if (value == null) {
//            value = supplier.get();
//            put(key,value,seconds);
//        }
//        return value;
//    }

    // 策略点
    public abstract String get(String key);
    public abstract void put(String key, String value,long seconds);

}
