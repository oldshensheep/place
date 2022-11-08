package com.oldshensheep.place.repo.impl;

import com.oldshensheep.place.repo.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;

@Primary
@RequiredArgsConstructor
public class RedisPlaceRepo implements PlaceRepository {

    private final RedisTemplate<String, byte[]> redisTemplate;


    //    @Value("#{app.placeRedisKey}")
    private final String PLACE_REDIS_KEY = "PLACE";

    public void setOne(byte[] value, Integer offset) {
        redisTemplate.opsForValue().set(PLACE_REDIS_KEY, value, offset);
    }

    public byte[] getOne(int offset, int length) {
        String longs = redisTemplate.opsForValue().get(PLACE_REDIS_KEY, offset, offset + length);
        if (longs != null) {
            byte[] bytes = longs.getBytes();
            return Arrays.copyOfRange(bytes, 0, length);
        } else {
            throw new IllegalStateException("no value at %s".formatted(offset));
        }
    }

    public byte[] getAll() {
        return redisTemplate.opsForValue().get(PLACE_REDIS_KEY);
    }

    public void setAll(byte[] value) {
        redisTemplate.opsForValue().set(PLACE_REDIS_KEY, value);
    }


}
