package br.com.joaoborges.filemanager.config;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.common.util.concurrent.RateLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Rate Limiting Interceptor
 *
 * Implements per-IP rate limiting to prevent API abuse.
 * Uses Google Guava's RateLimiter for token bucket algorithm.
 *
 * Configuration:
 * - Default: 10 requests per second per IP
 * - Automatic rate limiter creation per IP
 * - Returns 429 Too Many Requests when limit exceeded
 *
 * Rate limiting prevents:
 * - API abuse and DoS attacks
 * - Resource exhaustion
 * - Excessive server load
 */
@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    /**
     * Rate limiters per IP address
     * Key: IP address
     * Value: RateLimiter instance
     */
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * Default rate limit: 10 requests per second
     */
    private static final double REQUESTS_PER_SECOND = 10.0;

    /**
     * Intercept requests and apply rate limiting
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param handler Request handler
     * @return true if request allowed, false if rate limited
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Get client IP address
        String clientIp = getClientIP(request);

        // Get or create rate limiter for this IP
        RateLimiter rateLimiter = limiters.computeIfAbsent(clientIp,
            ip -> RateLimiter.create(REQUESTS_PER_SECOND));

        // Try to acquire permit
        if (rateLimiter.tryAcquire()) {
            // Request allowed
            return true;
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for IP: {}", clientIp);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"success\": false, \"message\": \"Rate limit exceeded. Maximum %s requests per second.\"}",
                (int) REQUESTS_PER_SECOND
            ));

            return false;
        }
    }

    /**
     * Extract client IP address from request
     * Checks X-Forwarded-For header for proxied requests
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
