package com.vianet.azure.sdk.manage.support.cache;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chen.rui on 6/7/2016.
 */
public class NullCacheManage implements CacheManager {

    private ConcurrentHashMap<String, NullCache> cacheConcurrentHashMap = new ConcurrentHashMap<String, NullCache>();

    @Override
    public Cache getCache(String name) {
        if(cacheConcurrentHashMap.get(name) == null) {
            cacheConcurrentHashMap.put(name, new NullCache());
        }
        return cacheConcurrentHashMap.get(name);

    }
}
