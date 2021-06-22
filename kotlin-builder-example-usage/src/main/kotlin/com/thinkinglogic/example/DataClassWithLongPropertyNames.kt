package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.DefaultValue
import java.time.LocalDate

@Builder
data class DataClassWithLongPropertyNames(
        @DefaultValue("myDefault") val stringWithAVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongNameThatWouldCauseLineWrappingInTheGeneratedFile: String = "myDefault",
        val nullableString: String?
) {

    /**
     * @return a Builder initialised with fields from this object.
     */
    fun toBuilder() = DataClassWithLongPropertyNamesBuilder(this)

    companion object {
        @JvmStatic
        fun builder() = DataClassWithLongPropertyNamesBuilder()
    }
}
