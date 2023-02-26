package com.thinkinglogic.example

import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.DefaultValue

class ClassWithSecondaryConstructor(
    val fullName: String,
    val otherName: String?) {
@Builder
constructor(
        forename: String,
        @DefaultValue("Anon") surname: String = "Anon",
        otherName: String?
): this(fullName = "$forename $surname", otherName = otherName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassWithSecondaryConstructor

        if (otherName != other.otherName) return false
        if (fullName != other.fullName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = otherName?.hashCode() ?: 0
        result = 31 * result + fullName.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClassWithSecondaryConstructor(otherName=$otherName, fullName='$fullName')"
    }
}