package com.viettel.deliverymanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Lightweight per-instance protection for the anonymous tracking endpoint. */
@Component
public class TrackingRateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = Duration.ofMinutes(1).toMillis();
    private static final int MAX_REQUESTS_PER_WINDOW = 30;
    private final Map<String, Window> requests = new ConcurrentHashMap<>();
    private final AtomicLong cleanupCounter = new AtomicLong();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/tracking/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        Window window = requests.compute(clientKey(request), (key, current) -> {
            if (current == null || now - current.startedAt() >= WINDOW_MILLIS) {
                return new Window(now, 1);
            }
            return new Window(current.startedAt(), current.count() + 1);
        });

        if ((cleanupCounter.incrementAndGet() & 255) == 0) {
            requests.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= WINDOW_MILLIS);
        }

        if (window.count() > MAX_REQUESTS_PER_WINDOW) {
            response.setStatus(429);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"code\":\"RATE_LIMITED\",\"message\":\"Bạn tra cứu quá nhanh. Vui lòng thử lại sau ít phút.\",\"data\":null}"
            );
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private record Window(long startedAt, int count) {}
}
