package com.thinkinglogic.builder.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.thinkinglogic.builder.annotation.Builder

class BuilderInfo(symbol: KSAnnotated, options: Map<String, String>) {
    val classDeclaration: KSClassDeclaration
    val constructor: KSFunctionDeclaration
    val setterPrefix: String
    val builderName: String

    init {
        when (symbol) {
            is KSFunctionDeclaration -> {
                if (symbol.parentDeclaration is KSClassDeclaration) {
                    this.classDeclaration = symbol.parentDeclaration as KSClassDeclaration
                    this.constructor = symbol
                } else {
                    throw IllegalArgumentException("Unable to find parent class for $symbol")
                }
            }
            is KSClassDeclaration -> {
                this.classDeclaration = symbol
                this.constructor = symbol.primaryConstructor!!
            }
            else -> throw IllegalArgumentException("Found annotation on unexpected symbol: $symbol")
        }
        this.setterPrefix = symbol.setterPrefix(options)
        this.builderName = "${targetName()}Builder"
    }

    private fun targetName() = fullyQualifiedTargetName()
        .removePrefix("${packageName()}.")
        .replace('.', '_')

    fun packageName() = classDeclaration.containingFile!!.packageName.asString()

    fun fullyQualifiedTargetName() = classDeclaration.fullyQualifiedName()
}

private fun KSDeclaration.fullyQualifiedName() =
    "${this.qualifiedName?.getQualifier()}.${this.qualifiedName?.getShortName()}"

private fun KSAnnotated.setterPrefix(options: Map<String, String>): String {
    annotations
        .filter { it.shortName.asString() == Builder::class.simpleName }
        .flatMap { it.arguments }
        .filter { it.name?.asString() == "setterPrefix" && it.value.toString() != "Use global settings" }
        .forEach { return it.value.toString() }
    return options["builder.setterPrefix"] ?: ""
}