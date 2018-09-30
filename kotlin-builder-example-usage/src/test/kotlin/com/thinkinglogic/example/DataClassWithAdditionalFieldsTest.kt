package com.thinkinglogic.example

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

}