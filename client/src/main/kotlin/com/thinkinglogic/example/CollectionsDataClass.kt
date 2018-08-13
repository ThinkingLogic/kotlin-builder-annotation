package com.thinkinglogic.example

import java.util.*

data class CollectionsDataClass(
        val listOfStrings: List<String>,
        val arrayOfStrings: Array<String>,
        val mapOfStrings: Map<String, String>

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CollectionsDataClass

        if (listOfStrings != other.listOfStrings) return false
        if (!Arrays.equals(arrayOfStrings, other.arrayOfStrings)) return false
        if (mapOfStrings != other.mapOfStrings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = listOfStrings.hashCode()
        result = 31 * result + Arrays.hashCode(arrayOfStrings)
        result = 31 * result + mapOfStrings.hashCode()
        return result
    }
}