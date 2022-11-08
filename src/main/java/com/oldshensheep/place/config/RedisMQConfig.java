package com.oldshensheep.place.config;

import com.oldshensheep.place.service.MQService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisMQConfig {
    @Bean
    RedisMessageListenerContainer container(
            RedisConnectionFactory connectionFactory,
            MQService redisMessageReceiver,
            ChannelTopic channelTopic,
            AvailableService availableService
    ) {
        if (!availableService.isRedisAvailable()) return null;
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(redisMessageReceiver);
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, channelTopic);
        return container;
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic("messageQueue");
    }
}
