package com.oldshensheep.place.config;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Slf4j
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    public Integer width = 1000;

    public Integer height = 1000;

    public String backupRate = "300";

    public String initImage = "init.png";

    public String token = UUID.randomUUID().toString();

    public Boolean useRedis = true;

    public Long rateLimit = 7L;

    public String backupFileName = "image_bitmap_backup.bin";

    public void setToken(String token) {
        if (token == null || token.length() <= 6) {
            this.token = UUID.randomUUID().toString();
            log.warn("invalid token %s, random uuid used".formatted(token));
        } else {
            this.token = token;
        }
    }

    public int getByteNum() {
        return this.height * this.width * 4;
    }

}
