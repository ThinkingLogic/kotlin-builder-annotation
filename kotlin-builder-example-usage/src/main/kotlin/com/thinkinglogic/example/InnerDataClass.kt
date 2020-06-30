package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder

@Builder
data class InnerDataClass(
        val constructorString: String,
        private val privateString: String
) {
    @Builder
    data class DataClassInDataClass(val constructorString: String,
                                    private val privateString: String)


    data class DataClassInDataClassWithConstructor @Builder constructor(val constructorString: String,
                                                                        private val privateString: String)

}
