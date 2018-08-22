package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import java.time.LocalDate
import java.util.*

@Builder
data class CollectionsDataClass(
        val listOfStrings: List<String>,
        // todo val mutableListOfStrings: MutableList<String>,
        val setOfLongs: Set<Long>,
        // todo: val setOfNullableLongs: Set<Long?>,
        val hashSet: HashSet<Long>,
        val collectionOfDates: Collection<LocalDate>,
        // todo val mapOfStringToDates: Map<String, LocalDate>,
        val treeMap: TreeMap<String, LocalDate>

)
