package com.thinkinglogic.example

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isNotNull
import assertk.assertions.message
import assertk.catch
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
                .withNotNullString(expected.notNullString)
                .withNotNullLong(expected.notNullLong)
                .withNullableLong(expected.nullableLong)
                .withDate(expected.date)
                .withValue(expected.value)
                .withStringWithDefault(expected.stringWithDefault)
                .withDefaultDate(expected.defaultDate)
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
                .withNotNullString(expected.notNullString)
                .withNotNullLong(expected.notNullLong)
                .withNullableLong(expected.nullableLong)
                .withDate(expected.date)
                .withValue(expected.value)
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
                .withNotNullString(newStringValue)
                .build()

        // then
        assertThat(actual).isNotEqualTo(original)
        assertThat(actual.notNullString).isEqualTo(newStringValue)
    }

    @Test
    fun `build method should throw exception if required property not set`() {
        // given
        val builder = SimpleDataClassBuilder()

        // when
        val expected = catch { builder.build() }

        // then
        assert(expected).isNotNull { e ->
            e is IllegalStateException
            e.message().isNotNull { it.contains("notNullString") }
        }

    }
}