package com.thinkinglogic.example

import assertk.assertions.isEqualTo
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
                .setOfLongs(expected.setOfLongs)
                .setOfNullableLongs(expected.setOfNullableLongs)
                .treeMap(expected.treeMap)
                .mapOfStringToNullableDates(expected.mapOfStringToNullableDates)
                .build()

        // then
        assertk.assert(actual).isEqualTo(expected)
    }

}