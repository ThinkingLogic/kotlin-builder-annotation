package com.thinkinglogic.builder.annotation

/**
 * A lightweight replacement for Lombok's @Builder annotation for Kotlin,
 * useful if your kotlin classes will be constructed in Java code.
 * Decorating a class or constructor with @Builder will cause an `{AnnotatedClassName}Builder` class to be generated.
 * Only applicable to Kotlin classes.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
annotation class Builder(
    /**
     * By default, setter methods in the builder are just the name of the property being set.
     * This can be changed on a global level by adding a 'builder.setterPrefix' option as a ksp arg, e.g. (in build.gradle):
     * ```
     *   ksp {
     *       arg("builder.setterPrefix" = "with")
     *   }
     * ```
     * It can also be specified here for an individual builder. */
    val setterPrefix: String = "Use global settings"
)

/**
 * Use this annotation to mark a collection or array as being allowed to contain null values,
 * As knowledge of the nullability is otherwise lost during annotation processing.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Deprecated("No longer necessary,nor used")
annotation class NullableType

/**
 * Use this annotation to mark a MutableList, MutableSet, MutableCollection, MutableMap, or MutableIterator,
 * as knowledge of their mutability is otherwise lost during annotation processing.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Deprecated("No longer necessary,nor used")
annotation class Mutable

/**
 * Use this annotation to provide a default value for the builder,
 * as knowledge of default values is otherwise lost during annotation processing.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Deprecated("No longer necessary,nor used")
annotation class DefaultValue(val value: String = "")