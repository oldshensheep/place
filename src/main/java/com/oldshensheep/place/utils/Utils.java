package com.oldshensheep.place.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

public class Utils {

    public static byte[] scaleImageTo(BufferedImage bufferedImage, int width, int height) {

        // fuck argb order, java ImageIO only supports argb order by default
        // var image = new BufferedImage(appConfig.width, appConfig.height, BufferedImage.TYPE_4BYTE_ABGR);
        // copy from https://stackoverflow.com/questions/65569243/getting-a-rgba-byte-array-from-a-bufferedimage-java
        var colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        var raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * 4, 4, new int[]{0, 1, 2, 3}, null);
        var image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);

        var graphics = image.getGraphics();
        graphics.drawImage(bufferedImage, 0, 0, width, height, null);
        graphics.dispose();

        return ((DataBufferByte) image.getData().getDataBuffer()).getData();
    }

    public static String getClientIP(HttpServletRequest request) {
        String remoteAddr = request.getHeader("CF-Connecting-IP");
        if (remoteAddr == null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
        }
        if (remoteAddr == null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
        }
        return remoteAddr;
    }
}
