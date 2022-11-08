package com.oldshensheep.place.repo.impl;

import com.oldshensheep.place.config.AppConfig;
import com.oldshensheep.place.repo.PlaceRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MemPlaceRepo implements PlaceRepository {

    private final byte[] data;
    private final String BACKUP_FILE_NAME = "image_bitmap_backup.bin";

    private final AppConfig appConfig;

    public MemPlaceRepo(AppConfig appConfig) {
        this.appConfig = appConfig;
        data = new byte[appConfig.getByteNum()];
        init();
    }

    private void init() {
        File file = new File(BACKUP_FILE_NAME);
        if (file.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                log.info("Reading data from backup file: %s".formatted(BACKUP_FILE_NAME));
                if (bytes.length > appConfig.getByteNum()) {
                    System.arraycopy(bytes, 0, data, 0, appConfig.getByteNum());
                    log.warn("backup data is too large: %s bytes, cut off to %s bytes"
                            .formatted(bytes.length, appConfig.getByteNum()));
                } else {
                    System.arraycopy(bytes, 0, data, 0, bytes.length);
                }
            } catch (IOException e) {
                log.error("reading backup file: %s".formatted(BACKUP_FILE_NAME), e);
            }
        }
    }

    @Override
    public void setOne(byte[] value, Integer offset) {
        System.arraycopy(value, 0, data, offset, value.length);
    }

    @Override
    public byte[] getOne(int offset, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(data, offset, bytes, 0, length);
        return bytes;
    }

    @Override
    public byte[] getAll() {
        return data;
    }

    @Override
    public void setAll(byte[] value) {
        System.arraycopy(value, 0, data, 0, value.length);
    }


    @PreDestroy
    @Scheduled(fixedRateString = "#{appConfig.backupRate}", timeUnit = TimeUnit.SECONDS)
    void backup() {
        try (FileOutputStream fos = new FileOutputStream(BACKUP_FILE_NAME)) {
            fos.write(data);
            log.info("backup data to %s".formatted(BACKUP_FILE_NAME));
        } catch (IOException e) {
            log.error("backup data to %s".formatted(BACKUP_FILE_NAME), e);
        }
    }
}
