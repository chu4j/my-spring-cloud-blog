package org.zhuqigong.blogservice.anotations;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zhuqigong.blogservice.model.Post;

import java.util.Collection;

@Component
public class CacheAnnotationHandler {
    private static final Logger log = LoggerFactory.getLogger(CacheAnnotationHandler.class);
    private final Cache<String, Collection<Post>> cache;

    public CacheAnnotationHandler(Cache<String, Collection<Post>> cache) {
        this.cache = cache;
    }

    public void handleInvalidCache() {
        cache.invalidateAll();
        cache.cleanUp();
        log.info("Invalid date all caffeine cache done");
    }
}
