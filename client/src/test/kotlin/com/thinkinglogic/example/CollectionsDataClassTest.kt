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
                treeMap = TreeMap()
        )

        // when
        val actual = CollectionsDataClassBuilder()
                .hashSet(expected.hashSet)
                .collectionOfDates(expected.collectionOfDates)
                .listOfStrings(expected.listOfStrings)
                .setOfLongs(expected.setOfLongs)
                .treeMap(expected.treeMap)
                .build()

        // then
        assertk.assert(actual).isEqualTo(expected)
    }

}