package io.github.valossa515.flow_guard_service.domain.enums;

public enum HttpMethodType {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS;

    public static boolean isValid(String method) {
        if (method == null) return false;
        try {
            valueOf(method.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
