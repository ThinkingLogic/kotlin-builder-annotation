package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder

@Builder
data class DataClassWithAdditionalFields(
        val constructorString: String,
        private val privateString: String
) {
    val nonConstructorString = constructorString + "foo"

}
