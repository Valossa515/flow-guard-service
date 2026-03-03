package io.github.valossa515.flow_guard_service.config;

public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    public static final String RATE_LIMIT_PREFIX = "rate-limit:";
    public static final String RATE_LIMIT_RULE_PREFIX = "rate-limit:rule:";

    public static String buildCounterKey(String clientId, String endpoint, String httpMethod) {
        return RATE_LIMIT_PREFIX + clientId + ":" + endpoint + ":" + httpMethod;
    }

    public static String buildRuleKey(String clientId, String endpoint, String method) {
        return RATE_LIMIT_RULE_PREFIX + clientId + ":" + endpoint + ":" + method;
    }
}
