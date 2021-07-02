package org.zhuqigong.blogservice.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zhuqigong.blogservice.model.Post;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {
    @Bean(name = "cachePostElements")
    public Cache<String, List<Post>> caffeineConfig() {
        return Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.HOURS).maximumSize(500).build();
    }

    @Bean(name = "cachePostElement")
    public Cache<String, Post> caffeineConfig2() {
        return Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.HOURS).maximumSize(500).build();
    }
}
