package com.thinkinglogic.example

import assertk.assert
import assertk.assertions.*
import assertk.catch
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
                date = LocalDate.now()
        )

        // when
        val actual = SimpleDataClassBuilder()
                .notNullString(expected.notNullString)
                .notNullLong(expected.notNullLong)
                .nullableLong(expected.nullableLong)
                .date(expected.date)
                .stringWithDefault(expected.stringWithDefault)
                .defaultDate(expected.defaultDate)
                .build()

        // then
        assert(actual).isEqualTo(expected)
    }

    @Test
    fun `builder should create object with default properties`() {
        // given
        val expected = SimpleDataClass(
                notNullString = "not null",
                nullableString = null,
                notNullLong = 123,
                nullableLong = 345,
                date = LocalDate.now()
        )

        // when
        val actual = SimpleDataClassBuilder()
                .notNullString(expected.notNullString)
                .notNullLong(expected.notNullLong)
                .nullableLong(expected.nullableLong)
                .date(expected.date)
                .build()

        // then
        assert(actual).isEqualTo(expected)
    }

    @Test
    fun `build method should throw exception if required property not set`() {
        // given
        val builder = SimpleDataClassBuilder()

        // when
        var expected = catch { builder.build() }

        // then
        assert(expected).isNotNull { e ->
            e is IllegalStateException
            e.message().isNotNull { it.contains("notNullString") }
        }

    }
}