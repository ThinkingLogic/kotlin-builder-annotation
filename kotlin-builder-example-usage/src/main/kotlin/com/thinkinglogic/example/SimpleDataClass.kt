package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.DefaultValue
import java.time.LocalDate

@Builder(setterPrefix = "with")
data class SimpleDataClass(
        val notNullString: String,
        val nullableString: String?,
        val notNullLong: Long,
        val nullableLong: Long?,
        val date: LocalDate,
        val value: String,
        @DefaultValue("withDefaultValue") val stringWithDefault: String = "withDefaultValue",
        @DefaultValue("LocalDate.MIN") val defaultDate: LocalDate = LocalDate.MIN
) {

    /**
     * @return a Builder initialised with fields from this object.
     */
    fun toBuilder() = SimpleDataClassBuilder(this)

    companion object {
        @JvmStatic
        fun builder() = SimpleDataClassBuilder()
    }
}