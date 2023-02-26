package com.thinkinglogic.example;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class SimpleDataClassJavaTest {

    @Test
    void builderShouldRejectNullValueForRequiredFields() {
        // given
        SimpleDataClassBuilder builder = new SimpleDataClassBuilder();

        // when
        Throwable exception = catchThrowable(() -> builder.withNotNullString(null));

        // then
        then(exception)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("withNotNullString");
    }

    @Test
    void toBuilderShouldReturnInitialisedBuilder() {
        // given
        SimpleDataClass original = new SimpleDataClassBuilder()
                .withDate(LocalDate.now())
                .withNotNullString("Foo")
                .withNullableString("Bar")
                .withNotNullLong(123L)
                .withValue("valueProperty")
                .build();

        // when
        SimpleDataClass result = original.toBuilder().build();

        // then
        assertThat(result).isEqualTo(original);
    }

    @Test
    void staticBuilderMethodReturnsBuilder() {
        // given

        // when
        SimpleDataClassBuilder builder = SimpleDataClass.builder();

        // then
        assertThat(builder).isNotNull();
    }
}
