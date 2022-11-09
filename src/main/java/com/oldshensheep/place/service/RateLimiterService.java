package com.oldshensheep.place.service;

import com.oldshensheep.place.config.AvailableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Service
public class RateLimiterService {
    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, Instant> hashMap;
    Function<String, Long> p;
    private Duration resetTimeout = Duration.of(10, ChronoUnit.SECONDS);

    public RateLimiterService(AvailableService availableService, StringRedisTemplate redisTemplate) {
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
        if (resetTimeout.isZero()) {
            p = s -> -1L;
        }
    }

    public long shouldLimit(String key) {
        return p.apply(key);
    }

    public Function<String, Long> redis() {
        return key -> {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                return redisTemplate.getExpire(key);
            } else {
                redisTemplate.opsForValue().set(key, "");
                redisTemplate.expire(key, this.resetTimeout);
                return -1L;
            }
        };
    }

    public Function<String, Long> hashmap() {
        return key -> {
            var pre = hashMap.get(key);
            var now = Instant.now();

            if (pre != null) {
                long d = pre.plus(resetTimeout).getEpochSecond() - now.getEpochSecond();
                if (d <= 0) {
                    hashMap.put(key, now);
                }
                return d;
            } else {
                hashMap.put(key, now);
                return -1L;
            }
        };
    }
}