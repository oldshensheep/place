package com.oldshensheep.place.config;

import com.oldshensheep.place.repo.PlaceRepository;
import com.oldshensheep.place.repo.impl.MemPlaceRepo;
import com.oldshensheep.place.repo.impl.RedisPlaceRepo;
import com.oldshensheep.place.service.MQService;
import com.oldshensheep.place.service.impl.MemMQService;
import com.oldshensheep.place.service.impl.RedisMQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.RedisSerializer;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class Config {
    private final AvailableService availableService;

    @Bean
    public RedisTemplate<String, byte[]> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public PlaceRepository placeRepository(RedisTemplate<String, byte[]> redisTemplate, AppConfig appConfig) {
        if (availableService.isRedisAvailable()) {
            return new RedisPlaceRepo(redisTemplate);
        } else {
            log.warn("Could not connect to redis server, using memory to store image data instead");
            return new MemPlaceRepo(appConfig);
        }
    }

    @Bean
    public MQService mqService(StringRedisTemplate redisTemplate, ChannelTopic topic) {
        if (availableService.isRedisAvailable()) {
            return new RedisMQService(redisTemplate, topic);
        } else {
            log.warn("Could not connect to redis server, using memory to store image data instead");
            return new MemMQService();
        }
    }
}
