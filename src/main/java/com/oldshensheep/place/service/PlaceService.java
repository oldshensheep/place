package com.oldshensheep.place.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oldshensheep.place.config.AppConfig;
import com.oldshensheep.place.entity.Operation;
import com.oldshensheep.place.repo.OperationRepository;
import com.oldshensheep.place.repo.PlaceRepository;
import com.oldshensheep.place.repo.impl.MemPlaceRepo;
import com.oldshensheep.place.repo.impl.RedisPlaceRepo;
import com.oldshensheep.place.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

    private final int MinOPSize = 4;
    private final AppConfig appConfig;
    private final PlaceRepository placeRepository;
    private final MQService mqService;
    private final OperationRepository operationRepository;
    private final ObjectMapper objectMapper;
    private final String BACKUP_FILE_NAME = "image_bitmap_backup.bin";

    @PostConstruct
    public void initPlaceRepository() {
        var data = new byte[appConfig.getByteNum()];
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
        if (placeRepository instanceof MemPlaceRepo) {
            placeRepository.setAll(data);
        } else if (placeRepository instanceof RedisPlaceRepo) {
            if (placeRepository.getAll() == null || placeRepository.getAll().length == 0) {
                placeRepository.setAll(data);
            }
        }
    }

    public byte[] getAllPlaces() {
        return placeRepository.getAll();
    }


    public void setAllPlaces(byte[] allPlaces) {
        if (allPlaces.length != appConfig.getByteNum()) {
            throw new IllegalArgumentException("value must be a byte array of %s length".formatted(appConfig.getByteNum()));
        }
        placeRepository.setAll(allPlaces);
    }

    public void initialize() {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(appConfig.initImage));
        } catch (IOException e) {
            log.error("Error reading image file", e);
            throw new IllegalStateException("Error reading image file", e);
        }
        byte[] imageBytes = Utils.scaleImageTo(bufferedImage, appConfig.width, appConfig.height);
        placeRepository.setAll(imageBytes);
        mqService.producer("init");
    }


    public void setPixel(int x, int y, List<Integer> color, String ip) {
        int offset = (x + y * appConfig.width) * MinOPSize;
        var bytes = new byte[]{
                color.get(0).byteValue(),
                color.get(1).byteValue(),
                color.get(2).byteValue(),
                color.get(3).byteValue(),
        };
        placeRepository.setOne(bytes, offset);
        var sseEmitterResp = new SseEmitterResp(x, y, color);
        try {
            mqService.producer(objectMapper.writeValueAsString(sseEmitterResp));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // TODO store user in the palace info
        Operation entity = Operation.builder()
                .color(ByteBuffer.wrap(bytes).getInt())
                .offset(offset)
                .ip(ip)
                .user(null)
                .build();
        operationRepository.save(entity);
    }


    public byte[] getPixel(int x, int y) {
        int offset = (x + y * appConfig.width) * MinOPSize;
        return placeRepository.getOne(offset, MinOPSize);
    }

    @PreDestroy
    @Scheduled(fixedRate = 300, timeUnit = TimeUnit.SECONDS)
    void backup() {
        try (FileOutputStream fos = new FileOutputStream(BACKUP_FILE_NAME)) {
            fos.write(placeRepository.getAll());
            log.info("backup data to %s".formatted(BACKUP_FILE_NAME));
        } catch (IOException e) {
            log.error("backup data to %s".formatted(BACKUP_FILE_NAME), e);
        }
    }

    private record SseEmitterResp(
            Integer x, Integer y, List<Integer> color
    ) {
    }
}
