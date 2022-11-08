package com.oldshensheep.place.service.impl;

import com.oldshensheep.place.service.MQService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

@Slf4j
public class MemMQService implements MQService {
    // TODO queen size should be ?
    private static final Integer MQ_SIZE = 1000;
    private final ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(MQ_SIZE);

    private Consumer<String> consumer;

    public MemMQService() {
        run();
    }

    @Override
    public void producer(String message) {
        arrayBlockingQueue.add(message);
    }

    @Override
    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
    }


    void run() {
        Thread.startVirtualThread(
                () -> {
                    while (true) {
                        try {
                            String take = arrayBlockingQueue.take();
                            consumer.accept(take);
                            log.info("consumer accepted");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

}
