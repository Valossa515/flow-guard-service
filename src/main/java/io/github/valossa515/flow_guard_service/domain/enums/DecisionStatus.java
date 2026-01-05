package io.github.valossa515.flow_guard_service.domain.enums;

public enum DecisionStatus {
    ALLOWED,
    RATE_LIMIT_EXCEEDED,
    CLIENT_NOT_FOUND,
    RULE_NOT_FOUND
}