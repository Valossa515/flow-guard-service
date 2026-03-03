package io.github.valossa515.flow_guard_service.validation;

import io.github.valossa515.flow_guard_service.domain.enums.HttpMethodType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HttpMethodValidator implements ConstraintValidator<ValidHttpMethod, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return HttpMethodType.isValid(value);
    }
}
