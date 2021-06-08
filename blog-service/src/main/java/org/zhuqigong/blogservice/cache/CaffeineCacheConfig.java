package org.zhuqigong.blogservice.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zhuqigong.blogservice.model.Post;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {
    @Bean
    public Cache<String, Collection<Post>> caffeineConfig() {
        return Caffeine.newBuilder().expireAfterWrite(12, TimeUnit.HOURS).maximumSize(200).build();
    }
}
