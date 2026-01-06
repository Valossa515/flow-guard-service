package io.github.valossa515.flow_guard_service.controller;

import io.github.valossa515.flow_guard_service.dto.CreateRateLimitRuleRequest;
import io.github.valossa515.flow_guard_service.dto.RateLimitRuleResponse;
import io.github.valossa515.flow_guard_service.service.RateLimitAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rules")
public class RateLimitAdminController {

    private final RateLimitAdminService service;

    public RateLimitAdminController(RateLimitAdminService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateRateLimitRuleRequest request) {
        service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<RateLimitRuleResponse> get(
            @RequestParam String clientId,
            @RequestParam String endpoint,
            @RequestParam String method
    ) {
        return service.get(clientId, endpoint, method)
                .map(rule -> ResponseEntity.ok(RateLimitRuleResponse.from(rule)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestParam String clientId,
            @RequestParam String endpoint,
            @RequestParam String method
    ) {
        service.delete(clientId, endpoint, method);
        return ResponseEntity.noContent().build();
    }
}
