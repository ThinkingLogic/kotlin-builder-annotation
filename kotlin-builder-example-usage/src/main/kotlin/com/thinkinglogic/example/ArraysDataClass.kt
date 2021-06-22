package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.NullableType
import java.time.LocalDate

@Builder
data class ArraysDataClass(
        val arrayOfLongs: Array<Long>,
        val arrayOfStrings: Array<String>,
        @NullableType val arrayOfNullableStrings: Array<String?>,
        val arrayOfListOfStrings: Array<List<String>>,
        val arrayOfDates: Array<LocalDate>
) {

    // Due to the way the JVM uses instance equality for arrays, we should override equals and hashcode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArraysDataClass

        if (!arrayOfLongs.contentEquals(other.arrayOfLongs)) return false
        if (!arrayOfStrings.contentEquals(other.arrayOfStrings)) return false
        if (!arrayOfNullableStrings.contentEquals(other.arrayOfNullableStrings)) return false
        if (!arrayOfListOfStrings.contentEquals(other.arrayOfListOfStrings)) return false
        if (!arrayOfDates.contentEquals(other.arrayOfDates)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arrayOfLongs.contentHashCode()
        result = 31 * result + arrayOfStrings.contentHashCode()
        result = 31 * result + arrayOfNullableStrings.contentHashCode()
        result = 31 * result + arrayOfListOfStrings.contentHashCode()
        result = 31 * result + arrayOfDates.contentHashCode()
        return result
    }
}
