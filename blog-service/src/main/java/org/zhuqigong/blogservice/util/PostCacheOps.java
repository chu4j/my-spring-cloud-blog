package org.zhuqigong.blogservice.util;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.zhuqigong.blogservice.model.Post;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class PostCacheOps {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostCacheOps.class);
    private final HttpServletRequest request;
    @Resource
    @Qualifier("cachePostElements")
    private Cache<String, List<Post>> cacheElements;
    @Resource
    @Qualifier("cachePostElement")
    private Cache<String, Post> cacheElement;

    public PostCacheOps(HttpServletRequest request) {
        this.request = request;
    }


    public String generateKeyByRequestUrl() {
        StringBuilder urlSuffix = new StringBuilder();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            urlSuffix.append(parameterName).append(request.getParameterValues(parameterName)[0]);
        }
        String cacheKey = request.getRequestURL().toString() + urlSuffix;
        LOGGER.info("Current request cache key is {}", cacheKey);
        return cacheKey;
    }

    public List<Post> getElements(String key) {
        return cacheElements.get(key, x -> Collections.emptyList());
    }

    public Post getElement(String key) {
        return cacheElement.get(key, x -> null);
    }

    public void saveOrUpdateElements(String key, List<Post> value) {
        cacheElements.put(key, value);
    }

    public void saveOrUpdateElement(String key, Post value) {
        cacheElement.put(key, value);
    }
}
