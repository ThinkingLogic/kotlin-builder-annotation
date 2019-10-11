package com.thinkinglogic.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GenericTypeDataClassTest {

    @Test
    fun `builder should create object with correct properties`() {
        // given
        val expected = GenericTypeDataClass(
                myTypedObject = TypedObject("Foo")
        )

        // when
        val actual = GenericTypeDataClassBuilder()
                .myTypedObject(expected.myTypedObject)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

}