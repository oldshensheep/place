package com.oldshensheep.place.web;

import com.oldshensheep.place.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    public RateLimitFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
        rateLimiterService.setResetTimeout(Duration.of(7, ChronoUnit.SECONDS));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String remoteAddr = request.getRemoteAddr();
        log.debug(remoteAddr);
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        // TODO:
        if (method.equals("PUT") && requestURI.startsWith("/pixels")) {
            if (rateLimiterService.shouldLimit(remoteAddr)) {
                response.setStatus(429);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}