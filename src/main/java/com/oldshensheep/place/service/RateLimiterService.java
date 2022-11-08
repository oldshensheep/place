package com.oldshensheep.place.service;

import com.oldshensheep.place.config.AvailableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Service
public class RateLimiterService {
    private final RedisTemplate<String, byte[]> redisTemplate;
    private final ConcurrentHashMap<String, Instant> hashMap;
    Function<String, Boolean> p;
    private Duration resetTimeout = Duration.of(10, ChronoUnit.SECONDS);

    public RateLimiterService(RedisTemplate<String, byte[]> redisTemplate, AvailableService availableService) {
        this.redisTemplate = redisTemplate;
        if (availableService.isRedisAvailable()) {
            p = redis();
            hashMap = null;
        } else {
            p = hashmap();
            hashMap = new ConcurrentHashMap<>();
        }
    }

    public void setResetTimeout(Duration resetTimeout) {
        this.resetTimeout = resetTimeout;
    }

    public boolean shouldLimit(String key) {
        return p.apply(key);
    }

    public Function<String, Boolean> redis() {
        return key -> {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                return true;
            } else {
                redisTemplate.opsForSet().add(key);
                redisTemplate.expire(key, this.resetTimeout);
                return false;
            }
        };
    }

    public Function<String, Boolean> hashmap() {
        return key -> {
            var pre = hashMap.get(key);
            var now = Instant.now();
            if (pre != null && pre.plus(resetTimeout).compareTo(now) >= 0) {
                log.info("%s %s".formatted(key, now));
                return true;
            } else {
                hashMap.put(key, now);
                return false;
            }
        };
    }
}