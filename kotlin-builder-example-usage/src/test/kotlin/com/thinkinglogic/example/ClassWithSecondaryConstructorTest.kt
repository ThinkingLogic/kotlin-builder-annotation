package com.thinkinglogic.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ClassWithSecondaryConstructorTest {

    @Test
    fun `builder should create object using constructor args`() {
        // given
        val forename = "Jane"
        val surname = "Doe"
        val expected = ClassWithSecondaryConstructor(
            forename = forename,
            surname = surname,
            otherName = "unknown"
        )

        // when
        val actual = ClassWithSecondaryConstructorBuilder()
            .forename(forename)
            .surname(surname)
            .otherName(expected.otherName)
            .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `builder should use correct defaults`() {
        // given
        val forename = "Jane"
        val expected = ClassWithSecondaryConstructor(
            forename = forename,
            otherName = "unknown"
        )

        // when
        val actual = ClassWithSecondaryConstructorBuilder()
            .forename(forename)
            .otherName(expected.otherName)
            .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `builder should inherit values from source`() {
        // given
        val forename = "Jane"
        val surname = "Smith"
        val expected = ClassWithSecondaryConstructor(
            forename = forename,
            surname = surname,
            otherName = "Jayne"
        )

        // when
        val actual = ClassWithSecondaryConstructorBuilder(expected)
            .forename(forename)
            .surname(surname)
            .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

}