package com.thinkinglogic.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

internal class CollectionsDataClassTest {

    @Test
    fun `builder should create object with correct properties`() {
        // given
        val expected = CollectionsDataClass(
                hashSet = HashSet(setOf(1L, 2L)),
                collectionOfDates = listOf(LocalDate.now()),
                listOfStrings = listOf("Foo", "bar"),
                listOfNullableStrings = listOf("Foo", "bar", null, null),
                setOfLongs = setOf(3L, 4L),
                setOfNullableLongs = setOf(3L, null),
                treeMap = TreeMap(),
                mapOfStringToNullableDates = mapOf("foo" to LocalDate.MAX, "bar" to null)
        )

        // when
        val actual = CollectionsDataClassBuilder()
                .hashSet(expected.hashSet)
                .collectionOfDates(expected.collectionOfDates)
                .listOfStrings(expected.listOfStrings)
                .listOfNullableStrings(expected.listOfNullableStrings)
                .setOfLongs(expected.setOfLongs)
                .setOfNullableLongs(expected.setOfNullableLongs)
                .treeMap(expected.treeMap)
                .mapOfStringToNullableDates(expected.mapOfStringToNullableDates)
                .build()

        // then
        assertThat(actual).isEqualTo(expected)
    }

}