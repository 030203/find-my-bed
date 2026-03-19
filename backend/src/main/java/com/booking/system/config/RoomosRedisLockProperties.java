package com.booking.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RefreshScope
@ConfigurationProperties(prefix = "roomos.redis.lock")
public class RoomosRedisLockProperties {

    private Duration waitTime = Duration.ofSeconds(3);
    private Duration leaseTime = Duration.ofSeconds(10);

    public Duration getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Duration waitTime) {
        this.waitTime = waitTime;
    }

    public Duration getLeaseTime() {
        return leaseTime;
    }

    public void setLeaseTime(Duration leaseTime) {
        this.leaseTime = leaseTime;
    }
}
