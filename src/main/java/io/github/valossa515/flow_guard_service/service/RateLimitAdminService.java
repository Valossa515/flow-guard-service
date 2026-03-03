package io.github.valossa515.flow_guard_service.service;

import io.github.valossa515.flow_guard_service.dto.CreateRateLimitRuleRequest;
import io.github.valossa515.flow_guard_service.dto.RateLimitRule;
import io.github.valossa515.flow_guard_service.repository.RateLimitRuleRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RateLimitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAdminService.class);

    private final RateLimitRuleRedisRepository repository;

    public RateLimitAdminService(RateLimitRuleRedisRepository repository) {
        this.repository = repository;
    }

    public void create(CreateRateLimitRuleRequest request) {
        RateLimitRule rule = new RateLimitRule();
        rule.setClientId(request.getClientId());
        rule.setEndpoint(request.getEndpoint());
        rule.setHttpMethod(request.getHttpMethod());
        rule.setLimit(request.getLimit());
        rule.setWindowSeconds(request.getWindowSeconds());

        repository.save(rule);
        log.info("Created rate limit rule: clientId={}, endpoint={}, method={}",
                request.getClientId(), request.getEndpoint(), request.getHttpMethod());
    }

    public Optional<RateLimitRule> get(String clientId, String endpoint, String method) {
        return repository.find(clientId, endpoint, method);
    }

    public void delete(String clientId, String endpoint, String method) {
        repository.delete(clientId, endpoint, method);
        log.info("Deleted rate limit rule: clientId={}, endpoint={}, method={}",
                clientId, endpoint, method);
    }
}
