package com.oldshensheep.place.service;

import com.oldshensheep.place.config.AppConfig;
import com.oldshensheep.place.config.AvailableService;
import com.oldshensheep.place.repo.PlaceRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleService {
    private final PlaceRepository placeRepository;
    private final AppConfig appConfig;
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
    private final AvailableService availableService;

    @PostConstruct
    void s() {
        if (!availableService.isRedisAvailable()) {
            scheduler.scheduleAtFixedRate(this::backup, 10, 300, TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    void cleanup() {
        if (!availableService.isRedisAvailable()) {
            backup();
        }
    }

    void backup() {
        try (FileOutputStream fos = new FileOutputStream(appConfig.backupFileName)) {
            fos.write(placeRepository.getAll());
            log.info("backup data to %s".formatted(appConfig.backupFileName));
        } catch (IOException e) {
            log.error("backup data to %s".formatted(appConfig.backupFileName), e);
        }
    }
}
