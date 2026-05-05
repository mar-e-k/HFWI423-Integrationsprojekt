package de.fhdw.vendix.commons.spring.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

public final class RequestRateFilter extends OncePerRequestFilter {

    private final RedissonClient redissonClient;

    // TODO: Move to config
    private final long MAX_REQUESTS = 60;
    private final Duration RESET_TIME_WINDOW = Duration.ofSeconds(60);

    public RequestRateFilter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String identifier = getIdentifier(request);
        String rateLimiterKey = "rate_limit:" + identifier;

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);
        rateLimiter.trySetRate(RateType.OVERALL, MAX_REQUESTS, RESET_TIME_WINDOW);

        if (rateLimiter.tryAcquire()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
        }
    }

    private String getIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                return jwt.getSubject();
            } else if (authentication instanceof JwtAuthenticationToken jwtToken) {
                return jwtToken.getToken().getSubject();
            }
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor == null || xForwardedFor.isBlank() || "unknown".equalsIgnoreCase(xForwardedFor)) {
            return request.getRemoteAddr();
        } else {
            return xForwardedFor.split(",")[0].trim();
        }
    }
}