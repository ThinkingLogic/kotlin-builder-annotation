package com.thinkinglogic.example;

import org.junit.jupiter.api.Test;

import static java.lang.Long.MIN_VALUE;
import static java.time.LocalDate.MIN;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class SimpleDataClassJavaTest {

    private SimpleDataClassBuilder builder = new SimpleDataClassBuilder();

    @Test
    void builderShouldReturnAnInstanceOfTheAnnotatedClass() {
        // given
        Class<SimpleDataClass> expectedClass = SimpleDataClass.class;

        // when
        SimpleDataClass simpleDataClass = builder
                .notNullString("")
                .notNullLong(0L)
                .date(now())
                .build();

        // then
        then(simpleDataClass).isInstanceOf(expectedClass);
    }

    @Test
    void builderShouldSetAllProperties() {
        // given
        SimpleDataClass expected = aSimpleDataClass();

        // when
        SimpleDataClass simpleDataClass = builder
                .notNullString("notNullString")
                .nullableString("nullableString")
                .notNullLong(0L)
                .nullableLong(MIN_VALUE)
                .date(now())
                .stringWithDefault("withDefaultValue")
                .date(now())
                .build();

        // then
        then(simpleDataClass).isEqualTo(expected);
    }

    @Test
    void builderShouldSetDefaultValues() {
        // given
        SimpleDataClass expected = aSimpleDataClass();

        // when
        SimpleDataClass simpleDataClass = builder
                .notNullString("notNullString")
                .nullableString("nullableString")
                .notNullLong(0L)
                .nullableLong(MIN_VALUE)
                .date(now())
                .build();

        // then
        then(simpleDataClass).isEqualTo(expected);
    }

    @Test
    void builderShouldRejectNullValueForRequiredFields() {
        // given
        Class<IllegalArgumentException> exceptionClass = IllegalArgumentException.class;

        // when
        Throwable exception = catchThrowable(() -> builder.notNullString(null));

        // then
        then(exception)
                .isInstanceOf(exceptionClass)
                .hasMessageContaining("notNullString");
    }

    private SimpleDataClass aSimpleDataClass() {
        return new SimpleDataClass("notNullString", "nullableString", 0L, MIN_VALUE, now(), "withDefaultValue", MIN);
    }
}
