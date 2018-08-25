package com.thinkinglogic.example

import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

internal class DataClassWithAdditionalFieldsTest {

    @Test
    fun `builder should create object with correct properties`() {
        // given
        val expected = DataClassWithAdditionalFields(
                constructorString = "foobar "
        )

        // when
        val actual = DataClassWithAdditionalFieldsBuilder()
                .constructorString(expected.constructorString)
                .build()

        // then
        assertk.assert(actual).isEqualTo(expected)
    }

}