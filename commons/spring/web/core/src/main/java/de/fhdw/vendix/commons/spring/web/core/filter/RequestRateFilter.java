package de.fhdw.vendix.commons.spring.web.core.filter;

import de.fhdw.vendix.commons.spring.data.caching.RedissonLockKey;
import de.fhdw.vendix.commons.spring.web.core.config.RequestRateProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public final class RequestRateFilter extends OncePerRequestFilter {

    private final RedissonClient redissonClient;
    private final RequestRateProperties requestRateProperties;

    public RequestRateFilter(RedissonClient redissonClient, RequestRateProperties requestRateProperties) {
        this.redissonClient = redissonClient;
        this.requestRateProperties = requestRateProperties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!requestRateProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientAddress(request);
        String key = RedissonLockKey.RATE_LIMIT.toIdentifier(clientIp);
        RRateLimiter limiter = redissonClient.getRateLimiter(key);

        if (!limiter.isExists()) {
            limiter.trySetRate(
                    RateType.OVERALL,
                    requestRateProperties.maxRequests(),
                    requestRateProperties.resetTimeWindow()
            );
        }

        if (limiter.tryAcquire()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(requestRateProperties.resetTimeWindow().toSeconds()));
        response.getWriter().write("""
                            {
                              "error": "Too many requests. Please try again later."
                            }
                """);
    }

    private String resolveClientAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null
                && !xForwardedFor.isBlank()
                && !"unknown".equalsIgnoreCase(xForwardedFor)
        ) {
            // First IP in chain = original client
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}