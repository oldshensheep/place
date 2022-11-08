package com.oldshensheep.place.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oldshensheep.place.config.AppConfig;
import com.oldshensheep.place.entity.Operation;
import com.oldshensheep.place.repo.OperationRepository;
import com.oldshensheep.place.repo.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

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

    public byte[] getAllPlaces() {
        return placeRepository.getAll();
    }


    public void setAllPlaces(byte[] allPlaces) {
        if (allPlaces.length != MinOPSize) {
            throw new IllegalArgumentException("value must be a byte array of %s length".formatted(MinOPSize));
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

        // fuck argb order, java ImageIO only supports argb order by default
        // var image = new BufferedImage(appConfig.width, appConfig.height, BufferedImage.TYPE_4BYTE_ABGR);
        // copy from https://stackoverflow.com/questions/65569243/getting-a-rgba-byte-array-from-a-bufferedimage-java
        var colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        var raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, appConfig.width, appConfig.height, appConfig.width * 4, 4, new int[]{0, 1, 2, 3}, null);
        var image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);

        var graphics = image.getGraphics();
        graphics.drawImage(bufferedImage, 0, 0, appConfig.width, appConfig.height, null);
        graphics.dispose();

        var imageBytes = ((DataBufferByte) image.getData().getDataBuffer()).getData();

        placeRepository.setAll(imageBytes);

        mqService.producer("init");
    }


    public void setPixel(int x, int y, List<Integer> color) {
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
                .user(null)
                .build();
        operationRepository.save(entity);
    }


    public byte[] getPixel(int x, int y) {
        int offset = (x + y * appConfig.width) * MinOPSize;
        return placeRepository.getOne(offset, MinOPSize);
    }

    private record SseEmitterResp(
            Integer x, Integer y, List<Integer> color
    ) {
    }
}
