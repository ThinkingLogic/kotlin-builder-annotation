package com.thinkinglogic.example

import assertk.assertions.contains
import assertk.assertions.isNotNull
import assertk.assertions.message
import assertk.catch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InnerDataClassTest {

    @Test
    fun `builder should create sub object with correct properties`() {
        // given
        val privateString = "barfoo"
        val expected = InnerDataClass.DataClassInDataClass(
                constructorString = "foobar ",
                privateString = privateString
        )

        // when
        val actual = InnerDataClass_DataClassInDataClassBuilder()
                .constructorString(expected.constructorString)
                .privateString(privateString)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }


    @Test
    fun `builder should create sub object with correct properties from constructor`() {
        // given
        val privateString = "barfoo"
        val expected = InnerDataClass.DataClassInDataClassWithConstructor(
                constructorString = "foobar ",
                privateString = privateString
        )

        // when
        val actual = InnerDataClass_DataClassInDataClassWithConstructorBuilder()
                .constructorString(expected.constructorString)
                .privateString(privateString)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }


}
