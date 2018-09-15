package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.NullableType
import java.time.LocalDate
import java.util.*

@Builder
data class CollectionsDataClass(
        val listOfStrings: List<String>,
        val setOfLongs: Set<Long>,
        @NullableType val setOfNullableLongs: Set<Long?>,
        val hashSet: HashSet<Long>,
        val collectionOfDates: Collection<LocalDate>,
        @NullableType val mapOfStringToNullableDates: Map<String, LocalDate?>,
        val treeMap: TreeMap<String, LocalDate>

)
