package io.github.valossa515.flow_guard_service.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class HttpMethodValidatorTest {

    private HttpMethodValidator validator;

    @BeforeEach
    void setUp() {
        validator = new HttpMethodValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"})
    void shouldAcceptValidHttpMethods(String method) {
        assertThat(validator.isValid(method, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "post", "Put", "delete"})
    void shouldAcceptLowercaseHttpMethods(String method) {
        assertThat(validator.isValid(method, null)).isTrue();
    }

    @Test
    void shouldRejectNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void shouldRejectBlank() {
        assertThat(validator.isValid("", null)).isFalse();
        assertThat(validator.isValid("  ", null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "CONNECT", "TRACE", "FOO", "123"})
    void shouldRejectInvalidMethods(String method) {
        assertThat(validator.isValid(method, null)).isFalse();
    }
}
