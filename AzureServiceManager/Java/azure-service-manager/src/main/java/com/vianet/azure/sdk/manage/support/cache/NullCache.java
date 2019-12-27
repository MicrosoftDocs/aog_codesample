package com.vianet.azure.sdk.manage.support.cache;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chen.rui on 6/7/2016.
 */
public class NullCache implements Cache {

    private ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(Object key, Object object) {
        cache.put(key.toString(), object);
    }

}
