package com.oldshensheep.place.web;

import com.oldshensheep.place.config.AppConfig;
import com.oldshensheep.place.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiterService rateLimiterService;
    private final AppConfig appConfig;

    public RateLimitInterceptor(RateLimiterService rateLimiterService, AppConfig appConfig) {
        this.rateLimiterService = rateLimiterService;
        this.appConfig = appConfig;
        rateLimiterService.setResetTimeout(Duration.of(appConfig.rateLimit, ChronoUnit.SECONDS));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null) {
            remoteAddr = request.getRemoteAddr();
        }
        log.debug(remoteAddr);
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        if (method.equals("PUT") && requestURI.startsWith("/pixels")) {
            long limit = rateLimiterService.shouldLimit(remoteAddr);
            if (limit > 0) {
                response.setHeader("X-RateLimit-Reset", String.valueOf(limit));
                response.setStatus(429);
                return false;
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        response.setHeader("X-RateLimit-Reset", String.valueOf(appConfig.rateLimit));
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
