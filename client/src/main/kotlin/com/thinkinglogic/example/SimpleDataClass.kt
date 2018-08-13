package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import java.time.LocalDate

@Builder
data class SimpleDataClass(
        val notNullString: String = "withDefaultValue",
        val nullableString: String?,
        val notNullLong: Long,
        val nullableLong: Long?,
        val date: LocalDate
)