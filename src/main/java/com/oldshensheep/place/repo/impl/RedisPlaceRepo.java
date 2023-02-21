package com.oldshensheep.place.repo.impl;

import com.oldshensheep.place.repo.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class RedisPlaceRepo implements PlaceRepository {

    private final RedisTemplate<String, byte[]> redisTemplate;

    private final String PLACE_REDIS_KEY = "PLACE";

    public void setOne(byte[] value, Integer offset) {
        redisTemplate.opsForValue().set(PLACE_REDIS_KEY, value, offset);
    }

    public byte[] getOne(int offset, int length) {
        return redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.
                stringCommands()
                .getRange(PLACE_REDIS_KEY.getBytes(StandardCharsets.UTF_8), offset, offset + length - 1));
    }

    public byte[] getAll() {
        return redisTemplate.opsForValue().get(PLACE_REDIS_KEY);
    }

    public void setAll(byte[] value) {
        redisTemplate.opsForValue().set(PLACE_REDIS_KEY, value);
    }


}
