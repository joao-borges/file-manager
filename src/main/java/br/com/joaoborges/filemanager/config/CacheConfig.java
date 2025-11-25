package br.com.joaoborges.filemanager.config;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * Cache Configuration
 *
 * Configures Spring Cache for application-level caching.
 * Uses Caffeine as the cache provider with time-based expiration.
 *
 * Caching strategy:
 * - Directory listings: cached for 5 minutes
 * - Operation results: cached for 10 minutes (if repeated)
 * - Path validations: cached for 30 minutes
 *
 * Benefits:
 * - Reduced file system I/O for repeated operations
 * - Faster response times for cached data
 * - Lower server load
 * - Automatic eviction on expiration
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Configure cache manager with Caffeine
     *
     * Cache specifications:
     * - directoryListings: 5 min TTL, max 100 entries
     * - pathValidations: 30 min TTL, max 500 entries
     * - operationResults: 10 min TTL, max 50 entries
     *
     * @return Configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {
            @Override
            protected org.springframework.cache.Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(
                    name,
                    Caffeine.newBuilder()
                        .expireAfterWrite(getCacheTTL(name), TimeUnit.MINUTES)
                        .maximumSize(getCacheMaxSize(name))
                        .recordStats()
                        .build()
                        .asMap(),
                    false
                );
            }
        };

        cacheManager.setCacheNames(Arrays.asList(
            "directoryListings",
            "pathValidations",
            "operationResults"
        ));

        log.info("Cache manager configured with caches: directoryListings, pathValidations, operationResults");

        return cacheManager;
    }

    /**
     * Get TTL for cache by name
     */
    private long getCacheTTL(String cacheName) {
        return switch (cacheName) {
            case "directoryListings" -> 5;  // 5 minutes
            case "pathValidations" -> 30;    // 30 minutes
            case "operationResults" -> 10;   // 10 minutes
            default -> 5;                    // default 5 minutes
        };
    }

    /**
     * Get maximum size for cache by name
     */
    private long getCacheMaxSize(String cacheName) {
        return switch (cacheName) {
            case "directoryListings" -> 100;   // 100 entries
            case "pathValidations" -> 500;     // 500 entries
            case "operationResults" -> 50;     // 50 entries
            default -> 100;                    // default 100 entries
        };
    }
}
