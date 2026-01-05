package io.github.valossa515.flow_guard_service.service.impl;

import io.github.valossa515.flow_guard_service.domain.enums.DecisionStatus;
import io.github.valossa515.flow_guard_service.dto.RateLimitRequestDTO;
import io.github.valossa515.flow_guard_service.dto.RateLimitResponseDTO;
import io.github.valossa515.flow_guard_service.service.RateLimitService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService {
    private static final long LIMIT = 5;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    @Override
    public RateLimitResponseDTO checkRateLimit(RateLimitRequestDTO request) {
        String key = buildKey(request);

        Counter counter = counters.getOrDefault(key, new Counter(0, Instant.now()));

        if (isWindowExpired(counter)) {
            counter = new Counter(0, Instant.now());
        }

        if (counter.count >= LIMIT) {
            long resetInSeconds = WINDOW_SECONDS -
                    (Instant.now().getEpochSecond() - counter.windowStart.getEpochSecond());

            return new RateLimitResponseDTO(
                    false,
                    DecisionStatus.RATE_LIMIT_EXCEEDED.name(),
                    0,
                    Math.max(resetInSeconds, 0)
            );
        }

        counter.count++;
        counters.put(key, counter);

        long remaining = LIMIT - counter.count;

        return new RateLimitResponseDTO(
                true,
                DecisionStatus.ALLOWED.name(),
                remaining,
                WINDOW_SECONDS
        );
    }

    private boolean isWindowExpired(Counter counter) {
        return Instant.now().getEpochSecond() - counter.windowStart.getEpochSecond() >= WINDOW_SECONDS;
    }

    private String buildKey(RateLimitRequestDTO request) {
        return request.getClientId()
                + ":" + request.getEndpoint()
                + ":" + request.getHttpMethod();
    }

    private static class Counter {
        long count;
        Instant windowStart;

        Counter(long count, Instant windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}