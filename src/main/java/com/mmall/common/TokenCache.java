package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by apple on 2018/6/20.
 */
public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    public static final String TOKEN_PREFIX = "token_";

    // LRU算法
    private static LoadingCache<String, String> localCache =
            CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(100000).expireAfterAccess(12, TimeUnit.HOURS)
                    .build(new CacheLoader<String, String>() {
                        // 如果为空的话，就放入"null"字段
                        @Override
                        public String load(String s) throws Exception {
                            return "null";
                        }
                    });

    public static void putKey(String key, String value) {
        localCache.put(key, value);
    }

    public static String getKey(String key) {
        String value = null;
        try {
            value = localCache.get(key);
            if ("null".equals(value)) {
                return null;
            }
        } catch (Exception e) {
            logger.error("localCache get error", e);
        }
        return value;
    }


}
