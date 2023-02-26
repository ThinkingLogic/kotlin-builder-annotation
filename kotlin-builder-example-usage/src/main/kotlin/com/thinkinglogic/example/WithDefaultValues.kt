package com.thinkinglogic.test

import com.thinkinglogic.builder.annotation.Builder
import java.util.UUID
import java.util.UUID.randomUUID
import java.util.UUID.fromString as uuid

/**
 * Do not reformat this file! It is used for confirming that the builder can handle varied formatting.
 */
@Builder
class WithDefaultRandomValues1(val foo: String = String(UUID.randomUUID().toString().encodeToByteArray())
                                    , val bar: String = String(randomUUID().toString().encodeToByteArray()))

@Builder
class WithDefaultRandomValues2(val foo: String = String(randomUUID().toString().encodeToByteArray()), val bar: String = String(UUID.randomUUID().toString().encodeToByteArray()))

@Builder
class WithDefaultRandomValues3(val foo: String = String(com.thinkinglogic.util.UUID().toString().encodeToByteArray()))

@Builder
data class WithDefaultValues1(
    val foo: List<Char> = listOf(',', ')', '}', '"'),
    val bar: String = "com.foo.Bar.baz(\"\", '}')",
    val baz: String = "A string containing // comment characters"
)

@Builder
data class WithDefaultValues2(
    val foo: String = // comments should be ignored by the builder
        "90055b91-612d-4f36-a237-f352acaf72a7",
    val bar: String /** comments should be ignored by the builder */
    = "com.foo.Bar.baz(\"\") // comment",
    val baz: String = String(UUID.fromString("90055b91-612d-4f36-a237-f352acaf72a7") /* comments should be ignored by the builder */
        .toString()
        .encodeToByteArray())
)

@Builder
data class WithDefaultValuesReferringToOtherProperties(
    val foo: String,
    val bar: String = "bar $foo",
    val baz: String = "baz " + foo
)

@Builder
data class WithDefaultValuesUsingAliasedImports(
    val foo: UUID = uuid("90055b91-612d-4f36-a237-f352acaf72a7")
)

@Builder
data class WithDefaultValuesUsingFunctions(
    val foo: String = defaultFoo(),
    val foo2: String = curlyBrackets { "$foo, $bar" },
    val bar: String = defaultBar(),
    val baz: String = defaultBaz(foo, bar),
    val baz2: String = defaultBaz2 { a, b -> "defaultBaz2" }
) {
    companion object {
        fun defaultFoo() = "This is a default foo"
        private fun privateFunction() = "boo"
        fun curlyBrackets(fn: () -> String): String = fn.invoke()
        @JvmStatic fun defaultBaz(a: String, b: String) = "A default baz $a $b"
        internal fun defaultBaz2(fn: (String, String) -> String) = fn.invoke("a", "b")
    }
}

fun defaultBar() = "A default bar"
