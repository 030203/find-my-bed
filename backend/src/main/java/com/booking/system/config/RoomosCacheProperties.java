package com.booking.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RefreshScope
@ConfigurationProperties(prefix = "roomos.cache")
public class RoomosCacheProperties {

    private Duration defaultTtl = Duration.ofMinutes(10);
    private Duration featuredPropertyTtl = Duration.ofMinutes(10);
    private Duration topPropertyTtl = Duration.ofMinutes(5);
    private Duration propertyDetailTtl = Duration.ofMinutes(15);

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public Duration getFeaturedPropertyTtl() {
        return featuredPropertyTtl;
    }

    public void setFeaturedPropertyTtl(Duration featuredPropertyTtl) {
        this.featuredPropertyTtl = featuredPropertyTtl;
    }

    public Duration getTopPropertyTtl() {
        return topPropertyTtl;
    }

    public void setTopPropertyTtl(Duration topPropertyTtl) {
        this.topPropertyTtl = topPropertyTtl;
    }

    public Duration getPropertyDetailTtl() {
        return propertyDetailTtl;
    }

    public void setPropertyDetailTtl(Duration propertyDetailTtl) {
        this.propertyDetailTtl = propertyDetailTtl;
    }
}
