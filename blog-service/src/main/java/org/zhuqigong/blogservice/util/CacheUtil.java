package org.zhuqigong.blogservice.util;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Component;
import org.zhuqigong.blogservice.model.Post;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

@Component
public class CacheUtil {
    private final HttpServletRequest request;
    private final Cache<String, Collection<Post>> cache;

    public CacheUtil(HttpServletRequest request, Cache<String, Collection<Post>> cache) {
        this.request = request;
        this.cache = cache;
    }

    public String getCacheKey() {
        return request.getRequestURL().toString();
    }

    public Collection<Post> getCache(String key) {
        return cache.asMap().getOrDefault(key, Collections.emptyList());
    }

    public void storeCache(String key, Collection<Post> value) {
        cache.put(key, value);
    }
}
