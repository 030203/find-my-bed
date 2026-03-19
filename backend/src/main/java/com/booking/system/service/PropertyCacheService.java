package com.booking.system.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class PropertyCacheService {

    public static final String FEATURED_CACHE = "property:featured";
    public static final String TOP_CACHE = "property:top";
    public static final String DETAIL_CACHE = "property:detail";

    private final CacheManager cacheManager;

    public PropertyCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictPropertyDetail(Long propertyId) {
        if (propertyId == null) {
            return;
        }
        Cache cache = cacheManager.getCache(DETAIL_CACHE);
        if (cache != null) {
            cache.evict(propertyId);
        }
    }

    public void clearFeaturedProperties() {
        clear(FEATURED_CACHE);
    }

    public void clearTopProperties() {
        clear(TOP_CACHE);
    }

    public void clearPropertyReadCaches(Long propertyId) {
        evictPropertyDetail(propertyId);
        clearFeaturedProperties();
        clearTopProperties();
    }

    private void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
