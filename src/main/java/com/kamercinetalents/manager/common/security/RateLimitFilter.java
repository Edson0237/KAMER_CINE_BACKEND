package com.kamercinetalents.manager.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int LOGIN_MAX_REQUESTS = 10;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);
    private static final int SYNC_MAX_REQUESTS = 30;
    private static final Duration SYNC_WINDOW = Duration.ofMinutes(1);
    private static final int PUBLIC_FORM_MAX_REQUESTS = 5;
    private static final Duration PUBLIC_FORM_WINDOW = Duration.ofMinutes(10);

    private final Map<String, RateBucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, RateBucket> syncBuckets = new ConcurrentHashMap<>();
    private final Map<String, RateBucket> publicFormBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientKey = getClientKey(request);

        if (path.endsWith("/api/iam/auth/login")) {
            if (!checkRate(loginBuckets, clientKey, LOGIN_MAX_REQUESTS, LOGIN_WINDOW, response, "Trop de tentatives de connexion. Réessayez dans 1 minute.")) {
                return;
            }
        } else if (path.endsWith("/api/sync")) {
            if (!checkRate(syncBuckets, clientKey, SYNC_MAX_REQUESTS, SYNC_WINDOW, response, "Trop de requêtes de synchronisation. Réessayez dans 1 minute.")) {
                return;
            }
        } else if (path.endsWith("/api/public/candidatures") || path.endsWith("/api/public/contact")) {
            if (!checkRate(publicFormBuckets, clientKey, PUBLIC_FORM_MAX_REQUESTS, PUBLIC_FORM_WINDOW, response, "Trop de soumissions de formulaire. Réessayez dans 10 minutes.")) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkRate(
            Map<String, RateBucket> buckets,
            String clientKey,
            int maxRequests,
            Duration window,
            HttpServletResponse response,
            String message) throws IOException {

        RateBucket bucket = buckets.computeIfAbsent(clientKey, k -> new RateBucket());
        synchronized (bucket) {
            OffsetDateTime now = OffsetDateTime.now();
            if (bucket.windowStart == null || now.isAfter(bucket.windowStart.plus(window))) {
                bucket.windowStart = now;
                bucket.count.set(0);
            }
            int current = bucket.count.incrementAndGet();
            if (current > maxRequests) {
                log.warn("Rate limit dépassé pour {}: {} requêtes en {}s", clientKey, current, window.getSeconds());
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, message);
                pd.setTitle("Limite de débit dépassée");
                pd.setProperty("timestamp", OffsetDateTime.now());
                pd.setProperty("retryAfterSeconds", window.getSeconds());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(toJson(pd));
                return false;
            }
        }
        return true;
    }

    private String getClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String toJson(ProblemDetail pd) {
        return "{\"title\":\"" + pd.getTitle() + "\","
                + "\"status\":" + pd.getStatus() + ","
                + "\"detail\":\"" + pd.getDetail() + "\","
                + "\"timestamp\":\"" + OffsetDateTime.now() + "\"}";
    }

    private static class RateBucket {
        OffsetDateTime windowStart;
        final AtomicInteger count = new AtomicInteger(0);
    }
}
