package com.oldshensheep.place.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AvailableService {

    private final AppConfig appConfig;
    private final RedisConnectionFactory connectionFactory;

    public boolean isRedisAvailable() {
        if (!appConfig.useRedis) return false;
        try {
            connectionFactory.getConnection();
        } catch (RedisConnectionFailureException e) {
            return false;
        }
        return true;
    }
}
