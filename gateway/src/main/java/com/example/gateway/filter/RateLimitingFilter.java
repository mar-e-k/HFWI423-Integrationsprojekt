package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final int MAX_REQUESTS_PER_SECOND = 10;

    // IP -> [windowStartMs, requestCount]
    private final ConcurrentHashMap<String, long[]> counters = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        long now = System.currentTimeMillis();
        long[] window = counters.compute(ip, (k, v) -> {
            if (v == null || now - v[0] > 1000) {
                return new long[]{now, 1};
            }
            v[1]++;
            return v;
        });

        if (window[1] > MAX_REQUESTS_PER_SECOND) {
            log.warn("[RateLimit] IP {} ueberschreitet Limit ({} req/s)", ip, MAX_REQUESTS_PER_SECOND);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
