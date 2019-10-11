package com.thinkinglogic.example

import assertk.assertions.contains
import assertk.assertions.isNotNull
import assertk.assertions.message
import assertk.catch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DataClassWithAdditionalFieldsTest {

    @Test
    fun `builder should create object with correct properties`() {
        // given
        val privateString = "barfoo"
        val expected = DataClassWithAdditionalFields(
                constructorString = "foobar ",
                privateString = privateString
        )

        // when
        val actual = DataClassWithAdditionalFieldsBuilder()
                .constructorString(expected.constructorString)
                .privateString(privateString)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `builder should not inherit private properties from source object`() {
        // given
        val source = DataClassWithAdditionalFields(
                constructorString = "foobar ",
                privateString = "barfoo"
        )

        // when
        var expected = catch { DataClassWithAdditionalFieldsBuilder(source).build() }

        // then
        assertk.assert(expected).isNotNull { e ->
            e is IllegalStateException
            e.message().isNotNull { it.contains("privateString") }
        }
    }

}