package com.thinkinglogic.builder.annotation

/**
 * A lightweight replacement for Lombok's @Builder annotation, decorating a class with @Builder will cause a
 * {AnnotatedClassName}Builder class to be generated.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Builder

/**
 * Use this annotation to mark a collection or array as being allowed to contain null values.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class NullableType


