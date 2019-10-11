package com.thinkinglogic.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class MutableCollectionsDataClassTest {

    @Test
    fun `builder should create object with correct properties`() {
        // given
        val expected = MutableCollectionsDataClass(
                listOfStrings = mutableListOf("Foo", "bar"),
                listOfAny = mutableListOf(3L, "Foo"),
                setOfNullableLongs = mutableSetOf(3L, null),
                mapOfStringToNullableDates = mutableMapOf("foo" to LocalDate.MAX, "bar" to null),
                collectionOfLongs = mutableListOf(),
                nullableSetOfLongs = null
        )

        // when
        val actual = MutableCollectionsDataClassBuilder()
                .listOfStrings(expected.listOfStrings)
                .listOfAny(expected.listOfAny)
                .setOfNullableLongs(expected.setOfNullableLongs)
                .mapOfStringToNullableDates(expected.mapOfStringToNullableDates)
                .collectionOfLongs(expected.collectionOfLongs)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

}