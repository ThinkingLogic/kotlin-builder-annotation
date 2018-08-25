package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder

@Builder
data class DataClassWithAdditionalFields(
        val constructorString: String
) {
    val nonConstructorString = constructorString + "foo"

}
