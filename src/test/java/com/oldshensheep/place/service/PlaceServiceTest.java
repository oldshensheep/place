package com.oldshensheep.place.service;

import com.oldshensheep.place.config.AppConfig;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;


@SpringBootTest
class PlaceServiceTest {

    @Resource
    PlaceService placeService;
    @Resource
    AppConfig appConfig;

    @Test
    void getAllPlaces() {
        Assertions.assertDoesNotThrow(() -> placeService.getAllPlaces());
    }

    @Test
    void setAllPlaces() {
        Assertions.assertDoesNotThrow(() -> placeService.setAllPlaces(new byte[appConfig.getByteNum()]));
        Assertions.assertThrows(IllegalArgumentException.class, () -> placeService.setAllPlaces(new byte[appConfig.getByteNum() - 1]));
    }

    @Test
    void initialize() {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(appConfig.initImage));
        } catch (IOException e) {
            throw new IllegalStateException("Error reading image file", e);
        }
        Assertions.assertDoesNotThrow(() -> placeService.initialize(bufferedImage));
    }

    @Test
    void setPixel() {
        List<Integer> color = List.of(255, 100, 0, 100);
        var colorBytes = new byte[]{
                color.get(0).byteValue(),
                color.get(1).byteValue(),
                color.get(2).byteValue(),
                color.get(3).byteValue(),
        };
        placeService.setPixel(0, 0, color, "");
        byte[] pixel = placeService.getPixel(0, 0);
        Assertions.assertArrayEquals(colorBytes, pixel);
    }

    @Test
    void getPixel() {
    }
}