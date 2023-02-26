package com.thinkinglogic.example

import com.thinkinglogic.test.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class WithDefaultValuesTest {

    @Test
    fun `Builder should create default (random) values correctly`() {
        assertThatCode { WithDefaultRandomValues1Builder().build() }.doesNotThrowAnyException()
        assertThatCode { WithDefaultRandomValues2Builder().build() }.doesNotThrowAnyException()
        assertThatCode { WithDefaultRandomValues3Builder().build() }.doesNotThrowAnyException()
    }

    @Test
    fun `Builder should create default (non-random) values correctly`() {
        assertThat(WithDefaultValues1Builder().build()).isEqualTo(WithDefaultValues1())
        assertThat(WithDefaultValues2Builder().build()).isEqualTo(WithDefaultValues2())

        assertThat(WithDefaultValuesReferringToOtherPropertiesBuilder().foo("myFoo").build())
            .isEqualTo(WithDefaultValuesReferringToOtherProperties("myFoo"))

        assertThat(WithDefaultValuesUsingAliasedImportsBuilder().build())
            .isEqualTo(WithDefaultValuesUsingAliasedImports())

        assertThat(WithDefaultValuesUsingFunctionsBuilder().build())
            .isEqualTo(WithDefaultValuesUsingFunctions())
    }
}