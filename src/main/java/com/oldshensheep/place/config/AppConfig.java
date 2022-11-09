package com.oldshensheep.place.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    public Integer width = 1000;

    public Integer height = 1000;

    public String backupRate = "300";

    public String initImage = "init.png";

    public String token;

    public Boolean useRedis = true;

    public Long rateLimit = 7L;

    public int getByteNum() {
        return this.height * this.width * 4;
    }
}
