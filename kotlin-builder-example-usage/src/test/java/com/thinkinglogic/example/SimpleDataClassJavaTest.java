package com.thinkinglogic.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class SimpleDataClassJavaTest {

    @Test
    void builderShouldRejectNullValueForRequiredFields() {
        // given
        SimpleDataClassBuilder builder = new SimpleDataClassBuilder();

        // when
        Throwable exception = catchThrowable(() -> builder.notNullString(null));

        then(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("notNullString");
    }
}
