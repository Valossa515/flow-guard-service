package io.github.valossa515.flow_guard_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = HttpMethodValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHttpMethod {
    String message() default "httpMethod must be a valid HTTP method (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
