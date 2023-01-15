package com.oldshensheep.place;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class PlaceApplication {

    public static void main(String[] args) {
        System.setProperty("io.lettuce.core.jfr", "false");
        SpringApplication.run(PlaceApplication.class, args);
    }

}
