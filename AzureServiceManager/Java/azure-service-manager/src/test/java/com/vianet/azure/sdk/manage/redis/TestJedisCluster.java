package com.vianet.azure.sdk.manage.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;


public class TestJedisCluster {

    public static void main(String[] args) {
        JedisPoolConfig config = new JedisPoolConfig();

        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        jedisClusterNodes.add(new HostAndPort("kevin.redis.cache.chinacloudapi.cn", 6379));
        JedisCluster jc = new JedisCluster(jedisClusterNodes, 3000, 3000, 5, "rwRJ2iYeOeqMx5tZ5Bs6Ir4CmWNbxotkGwyndFfsnVg=", config);
        jc.set("foo", "bar");
        String value = jc.get("foo");
        System.out.println(value);
    }

}
