package com.oldshensheep.place.service.impl;

import com.oldshensheep.place.service.MQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class RedisMQService implements MQService, MessageListener {

    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic topic;
    private Consumer<String> consumer;

    @Override
    public void producer(String message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }

    @Override
    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        consumer.accept(message.toString());
        log.info("consumer accepted");
    }

}