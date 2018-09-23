package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.NullableType
import java.time.LocalDate
import java.util.*

@Builder
data class ArraysDataClass(
        val arrayOfLongs: Array<Long>,
        val arrayOfStrings: Array<String>,
        @NullableType val arrayOfNullableStrings: Array<String?>,
        val arrayOfListOfStrings: Array<List<String>>,
        val arrayOfDates: Array<LocalDate>

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArraysDataClass

        if (!Arrays.equals(arrayOfLongs, other.arrayOfLongs)) return false
        if (!Arrays.equals(arrayOfStrings, other.arrayOfStrings)) return false
        if (!Arrays.equals(arrayOfNullableStrings, other.arrayOfNullableStrings)) return false
        if (!Arrays.equals(arrayOfListOfStrings, other.arrayOfListOfStrings)) return false
        if (!Arrays.equals(arrayOfDates, other.arrayOfDates)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(arrayOfLongs)
        result = 31 * result + Arrays.hashCode(arrayOfStrings)
        result = 31 * result + Arrays.hashCode(arrayOfNullableStrings)
        result = 31 * result + Arrays.hashCode(arrayOfListOfStrings)
        result = 31 * result + Arrays.hashCode(arrayOfDates)
        return result
    }
}