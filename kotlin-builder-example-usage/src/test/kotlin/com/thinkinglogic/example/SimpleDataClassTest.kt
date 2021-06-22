package com.thinkinglogic.example

import assertk.assertions.isFailure
import assertk.assertions.messageContains
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SimpleDataClassTest {

    @Test
    fun `builder should create object with correct properties`() {
        // given
        val expected = SimpleDataClass(
                notNullString = "foo",
                nullableString = null,
                notNullLong = 123,
                nullableLong = 345,
                date = LocalDate.now(),
                value = "valueProperty"
        )

        // when
        val actual = SimpleDataClassBuilder()
                .notNullString(expected.notNullString)
                .notNullLong(expected.notNullLong)
                .nullableLong(expected.nullableLong)
                .date(expected.date)
                .value(expected.value)
                .stringWithDefault(expected.stringWithDefault)
                .defaultDate(expected.defaultDate)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `builder should create object with default properties`() {
        // given
        val expected = SimpleDataClass(
                notNullString = "not null",
                nullableString = null,
                notNullLong = 123,
                nullableLong = 345,
                date = LocalDate.now(),
                value = "valueProperty"
        )

        // when
        val actual = SimpleDataClassBuilder()
                .notNullString(expected.notNullString)
                .notNullLong(expected.notNullLong)
                .nullableLong(expected.nullableLong)
                .date(expected.date)
                .value(expected.value)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `builder should inherit properties from source`() {
        // given
        val expected = SimpleDataClass(
                notNullString = "not null",
                nullableString = null,
                notNullLong = 123,
                nullableLong = 345,
                date = LocalDate.now(),
                value = "valueProperty"
        )

        // when
        val actual = SimpleDataClassBuilder(expected)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `builder should replace inherited properties`() {
        // given
        val original = SimpleDataClass(
                notNullString = "not null",
                nullableString = null,
                notNullLong = 123,
                nullableLong = 345,
                date = LocalDate.now(),
                value = "valueProperty"
        )
        val newStringValue = "New value"

        // when
        val actual = SimpleDataClassBuilder(original)
                .notNullString(newStringValue)
                .build()

        // then
        assertThat(actual).isNotEqualTo(original)
        assertThat(actual.notNullString).isEqualTo(newStringValue)
    }

    @Test
    fun `build method should throw exception if required property not set`() {
        // given
        val builder = SimpleDataClassBuilder()

        // then
        assertk.assertThat { builder.build() }
          .isFailure()
          .messageContains("notNullString")
    }
}
