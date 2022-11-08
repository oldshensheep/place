package com.oldshensheep.place.service;

import java.util.function.Consumer;

public interface MQService {

    void producer(String message);

    void setConsumer(Consumer<String> consumer);
}
