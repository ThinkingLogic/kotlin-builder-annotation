package com.thinkinglogic.builder.annotation

/**
 * A lightweight replacement for Lombok's @Builder annotation, decorating a class with @Builder will cause a
 * {AnnotatedClassName}Builder class to be generated.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
annotation class Builder

/**
 * Use this annotation to mark a collection or array as being allowed to contain null values,
 * As knowledge of the nullability is otherwise lost during annotation processing.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class NullableType

/**
 * Use this annotation to mark a MutableList, MutableSet, MutableCollection, MutableMap, or MutableIterator,
 * as knowledge of their mutability is otherwise lost during annotation processing.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Mutable

/**
 * Use this annotation to provide a default value for the builder,
 * as knowledge of default values is otherwise lost during annotation processing.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class DefaultValue (val value: String = "")