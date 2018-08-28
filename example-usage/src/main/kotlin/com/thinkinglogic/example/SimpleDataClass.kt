package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.DefaultValue
import java.time.LocalDate

@Builder
data class SimpleDataClass(
        val notNullString: String,
        val nullableString: String?,
        val notNullLong: Long,
        val nullableLong: Long?,
        val date: LocalDate,
        @DefaultValue("withDefaultValue") val stringWithDefault: String = "withDefaultValue",
        @DefaultValue("LocalDate.MIN") val defaultDate: LocalDate = LocalDate.MIN
)