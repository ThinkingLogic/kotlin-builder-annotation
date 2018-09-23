package com.thinkinglogic.example

import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class ArraysDataClassTest {

    @Test
    fun `builder should create object with correct properties`() {
        // given
        val expected = ArraysDataClass(
                arrayOfDates = arrayOf(LocalDate.now(), LocalDate.MIN),
                arrayOfLongs = arrayOf(1L),
                arrayOfStrings = arrayOf("one", "two"),
                arrayOfNullableStrings = arrayOf("foo", null),
                arrayOfListOfStrings = arrayOf()
        )

        // when
        val actual = ArraysDataClassBuilder()
                .arrayOfDates(expected.arrayOfDates)
                .arrayOfLongs(expected.arrayOfLongs)
                .arrayOfStrings(expected.arrayOfStrings)
                .arrayOfListOfStrings(expected.arrayOfListOfStrings)
                .build()

        // then
        assertk.assert(actual).isEqualTo(expected)
    }

}