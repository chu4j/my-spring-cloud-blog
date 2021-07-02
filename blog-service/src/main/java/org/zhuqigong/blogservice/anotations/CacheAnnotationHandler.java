package org.zhuqigong.blogservice.anotations;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zhuqigong.blogservice.model.Post;

import java.util.List;

@Component
public class CacheAnnotationHandler {
    private static final Logger log = LoggerFactory.getLogger(CacheAnnotationHandler.class);
    private final Cache<String, List<Post>> cachePostElements;
    private final Cache<String, Post> cachePostElement;

    public CacheAnnotationHandler(Cache<String, List<Post>> cachePostElements, Cache<String, Post> cachePostElement) {
        this.cachePostElements = cachePostElements;
        this.cachePostElement = cachePostElement;
    }

    public void handleInvalidCacheElements() {
        cachePostElements.invalidateAll();
        cachePostElements.cleanUp();
        log.info("Invalid post elements success");
    }

    public void handleInvalidCacheElement() {
        cachePostElement.invalidateAll();
        cachePostElement.cleanUp();
        log.info("Invalid post element success");
    }
}
