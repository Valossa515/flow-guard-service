package io.github.valossa515.flow_guard_service.dto;

import lombok.Data;

@Data
public class RateLimitResponseDTO {
    private boolean allowed;
    private String decision;
    private long remainingRequests;
    private long resetInSeconds;

    public RateLimitResponseDTO(boolean allowed, String decision, long remainingRequests, long resetInSeconds) {
        this.allowed = allowed;
        this.decision = decision;
        this.remainingRequests = remainingRequests;
        this.resetInSeconds = resetInSeconds;
    }
}
