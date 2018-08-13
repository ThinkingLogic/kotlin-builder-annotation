package com.thinkinglogic.example

data class ConstructorArgDataClass(
        val nonPropertyParam: String,
        val otherProperty: String
) {
    private val propertyValue = nonPropertyParam
}