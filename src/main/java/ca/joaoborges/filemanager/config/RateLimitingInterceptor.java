package ca.joaoborges.filemanager.config;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
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
     * Per-IP request rate. Configurable via filemanager.rate-limit.requests-per-second
     * (default 10). Tests bump this to a high value via @TestPropertySource.
     */
    @Value("${filemanager.rate-limit.requests-per-second:10.0}")
    private double requestsPerSecond;

    /**
     * Intercept requests and apply rate limiting
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param handler Request handler
     * @return true if request allowed, false if rate limited
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler) throws Exception {

        final String clientIp = getClientIP(request);

        final RateLimiter rateLimiter = limiters.computeIfAbsent(clientIp,
            ip -> RateLimiter.create(requestsPerSecond));

        if (rateLimiter.tryAcquire()) {
            return true;
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"success\": false, \"message\": \"Rate limit exceeded. Maximum %s requests per second.\"}",
                (int) requestsPerSecond
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
    private String getClientIP(final HttpServletRequest request) {
        final String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
