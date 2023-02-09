package com.oldshensheep.place.web;

import com.oldshensheep.place.config.AppConfig;
import com.oldshensheep.place.service.MQService;
import com.oldshensheep.place.service.PlaceService;
import com.oldshensheep.place.utils.Utils;
import com.oldshensheep.place.web.request.PutPixelRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

@Slf4j
@RestController
public class Controller {
    private final AppConfig appConfig;
    private final PlaceService service;
    private final MQService mqService;

    private final ConcurrentHashMap<SseEmitter, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    public Controller(PlaceService service, MQService mqService, AppConfig appConfig) {
        this.service = service;
        this.mqService = mqService;
        this.appConfig = appConfig;
        initialize();
    }


    @GetMapping("/time")
    public SseEmitter data() {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitter.onCompletion(() -> {
            sseEmitters.remove(sseEmitter);
        });
        this.sseEmitters.put(sseEmitter, sseEmitter);
        log.info("new SseEmitter %s".formatted(sseEmitter));
        return sseEmitter;
    }

    @PutMapping("/pixels")
    public void putOne(@RequestBody PutPixelRequest pixelRequest, HttpServletRequest request) {
        var remoteAddr = Utils.getClientIP(request);
        ValidateUtils.Between(pixelRequest.x(), 0, appConfig.width);
        ValidateUtils.Between(pixelRequest.y(), 0, appConfig.height);
        ValidateUtils.Length(pixelRequest.color(), 4, 4);
        service.setPixel(pixelRequest.x(), pixelRequest.y(), pixelRequest.color(), remoteAddr);
    }

    @PostMapping("/init")
    public void putAll(@RequestParam String token, HttpServletResponse response) {
        if (token != null && token.equals(appConfig.token)) {
            BufferedImage bufferedImage;
            try {
                bufferedImage = ImageIO.read(new File(appConfig.initImage));
            } catch (IOException e) {
                log.error("Error reading image file", e);
                throw new IllegalStateException("Error reading image file", e);
            }
            service.initialize(bufferedImage);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * cloudflare is not support to custom compression, so I set the `Content-Type` to `text/plain` that will
     * enable compression for the request in the cloudflare server
     */
    @GetMapping(value = "/pixels/all")
    public ResponseEntity<byte[]> getAllPixel() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "text/plain");
        byte[] places = service.getAllPlaces();

        /**
         * https://www.redditinc.com/blog/how-we-built-rplace/
         * We decided to cache at the CDN (Fastly) layer because it was simple to implement
         * and it meant the cache was as close to clients as possible which would help response speed.
         * Requests for the full state of the board were cached by Fastly with an expiration of 1 second.
         * We also added the stale-while-revalidate cache control header option to prevent more requests
         * from falling through than we wanted when the cached board expired.
         */

        //TODO 缓存会带来一个问题, 消息推送的都是最新的被改变的像素. 缓存中会缺失部分在 缓存期间内进入的 像素.

        CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.SECONDS)
                .staleWhileRevalidate(1, TimeUnit.SECONDS);

        return ResponseEntity.ok()
                .headers(httpHeaders)
//                .cacheControl(cacheControl)
                .body(places);
    }

    @GetMapping("/pixels")
    public Object getPixel(@RequestParam Integer x, @RequestParam Integer y) {
        ValidateUtils.Between(x, 0, appConfig.width);
        ValidateUtils.Between(y, 0, appConfig.height);
        return service.getPixel(x, y);
    }

    private void initialize() {
        log.info("using token %s".formatted(appConfig.token));
        mqService.setConsumer(s -> {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (SseEmitter sseEmitter : sseEmitters.keySet()) {
                    executor.submit(() -> {
                        try {
                            sseEmitter.send(event().name("newPixel").data(s));
                            log.info("Send sseEmitter: %s".formatted(s));
                        } catch (IOException e) {
                            log.warn("IOException sseEmitter.send %s. Cased by %s".formatted(sseEmitter, e.getMessage()));
                        }
                    });
                }
            }
        });
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    private void heartbeat() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (SseEmitter sseEmitter : sseEmitters.keySet()) {
                executor.submit(() -> {
                    try {
                        sseEmitter.send(event().name("ping").data("ping"));
                        log.debug("%s, ping".formatted(sseEmitter));
                    } catch (IOException e) {
                        log.warn("IOException sseEmitter.send %s. Cased by %s".formatted(sseEmitter, e.getMessage()));
                    }
                });
            }
        }
    }

}
