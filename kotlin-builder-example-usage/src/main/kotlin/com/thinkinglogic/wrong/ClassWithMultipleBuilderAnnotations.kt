package com.thinkinglogic.wrong

import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.DefaultValue

@Builder
class ClassWithMultipleBuilderAnnotations(
    val fullName: String
) {
    @Builder
    constructor(
        forename: String,
        surname: String = "Anon",
    ) : this("$forename $surname")
    @Builder
    constructor(
        forename: String,
        surname: String = "Anon",
        middleName: String = ""
    ) : this("$forename $middleName $surname")
}