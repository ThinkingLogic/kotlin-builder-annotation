package com.thinkinglogic.builder.processor

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.thinkinglogic.builder.annotation.Builder
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter


class BuilderWriter(private val info: BuilderInfo, private val logger: Logger) {

    companion object {
        private const val CHECK_REQUIRED_FIELDS_FUNCTION_NAME = "checkRequiredFields"
        private const val AS_KEYWORD = " as "
    }

    private val targetClass = info.classDeclaration.toClassName()
    private val sourceFile: List<String> =
        readFileSourceIfDefaultsPresent(info) // we need to parse default values direct from source :(

    fun write(outputStream: OutputStream) {
        val builderSpec = TypeSpec.classBuilder(info.builderName)
        val builderClass = ClassName(info.packageName(), info.builderName)
        info.classDeclaration.containingFile?.let(builderSpec::addOriginatingKSFile)

        info.constructor.parameters.forEach { field ->
            logger.debug("Adding ${field.name?.getShortName()} to ${info.builderName}")
            builderSpec.addProperty(field.asProperty())
            builderSpec.addFunction(field.asSetterFunctionReturning(builderClass))
        }

        builderSpec.primaryConstructor(FunSpec.constructorBuilder().build()) // no-arg constructor
        builderSpec.addFunction(createCopyConstructor())
        builderSpec.addFunction(createBuildFunction())
        builderSpec.addFunction(createCheckRequiredFieldsFunction())

        val fileSpecBuilder = FileSpec
            .builder(info.packageName(), info.builderName)
            .addType(builderSpec.build())

        fileSpecBuilder.addImports()

        OutputStreamWriter(outputStream).use { writer ->
            fileSpecBuilder.build().writeTo(writer)
        }
    }

    private fun FileSpec.Builder.addImports() {
        fileImports().forEach { (pkg, names) ->
            val (aliased, nonAliased) = names.partition { it.contains(AS_KEYWORD) }
            if (nonAliased.isNotEmpty()) {
                this.addImport(pkg, nonAliased)
            }
            aliased.forEach {
                val className = it.substringBefore(AS_KEYWORD)
                val aliasName = it.substringAfter(AS_KEYWORD)
                this.addAliasedImport(ClassName(pkg, className), aliasName)
            }
        }
        companionImports().forEach { (pkg, names) -> this.addImport(pkg, names) }
    }

    private fun fileImports(): Map<String, List<String>> {
        return sourceFile.asSequence()
            .map(String::trim)
            .filter { it.startsWith("import") }
            .filter { !it.contains(Builder::class.java.canonicalName) }
            .map { it.substringAfter("import ").trim(';') }
            .map { it.substringBeforeLast('.') to it.substringAfterLast('.') }
            .groupBy { it.first }
            .mapValues { entry -> entry.value.map { pair -> pair.second } }
    }

    /** Returns a map (of package -> names) to import companion functions, but only if there are default values. */
    private fun companionImports(): Map<String, List<String>> {
        if (info.hasDefaultValues) {
            val companionObject = this.info.classDeclaration.declarations
                .filter { it is KSClassDeclaration && it.isCompanionObject }
                .map { it as KSClassDeclaration }
                .firstOrNull() ?: return emptyMap()
            val functionNames = companionObject.declarations
                .filter { it is KSFunctionDeclaration }
                .map { it as KSFunctionDeclaration }
                .filterNot { it.returnType?.toString() == "Companion" }
                .filterNot { it.modifiers.contains(Modifier.PRIVATE) }
                .map { it.simpleName.getShortName() }
                .toList()
            if (functionNames.isNotEmpty()) {
                val qualifiedName = companionObject.qualifiedName!!
                return mapOf("${qualifiedName.getQualifier()}.${qualifiedName.getShortName()}" to functionNames)
            }
        }
        return emptyMap()
    }

    /** Creates a mutable property with the type of this parameter and an initial value of null. */
    private fun KSValueParameter.asProperty(): PropertySpec {
        return PropertySpec.builder(name!!.getShortName(), asKotlinTypeName().copy(nullable = true), KModifier.PRIVATE)
            .mutable()
            .initializer("null")
            .build()
    }

    /** Creates a function that sets the property identified by this parameter, and returns the [builder]. */
    private fun KSValueParameter.asSetterFunctionReturning(builder: ClassName): FunSpec {
        val propertyName = name!!.getShortName()
        val setterName = name.withPrefix(info.setterPrefix)
        return FunSpec.builder(setterName)
            .addParameter(ParameterSpec.builder("newValue", asKotlinTypeName()).build())
            .returns(builder)
            .addCode("return apply·{ this.${propertyName}·=·newValue }\n")
            .build()
    }

    /** Creates a constructor that accepts an instance of the target class, from which default values are obtained. */
    private fun createCopyConstructor(): FunSpec {
        val source = "source"
        val targetProperties = info.classDeclaration.getAllProperties()
            .filterNot { it.modifiers.contains(Modifier.PRIVATE) }
            .map { it.simpleName.getShortName() }
            .toSet()
        val code = StringBuilder()
        info.constructor.parameters
            .filter { it.name?.getShortName() in targetProperties }
            .map { it.name!!.getShortName() }
            .forEach { field ->
                code.append("this.$field·=·$source.$field")
                    .appendLine()
            }
        return FunSpec.constructorBuilder()
            .addParameter(ParameterSpec.builder(source, targetClass).build())
            .callThisConstructor()
            .addCode(code.toString())
            .build()
    }


    /** Creates a 'build()' function that will invoke the target constructor and return the new instance. */
    private fun createBuildFunction(): FunSpec {
        val code = StringBuilder("$CHECK_REQUIRED_FIELDS_FUNCTION_NAME()").appendLine()
        info.constructor.parameters
            .filter { it.hasDefault }
            .forEach { field ->
                val simpleName = field.simpleName
                code.append("@Suppress(\"NAME_SHADOWING\")").appendLine()
                code.append("val $simpleName = this.$simpleName ?: ")
                    .append("(")
                    .append(field.defaultValue())
                    .append(")")
                    .appendLine()
            }
        code.appendLine()
            .append("return·${targetClass.canonicalName.removePrefix("${this.info.packageName()}.")}(")
        val iterator = info.constructor.parameters.listIterator()
        while (iterator.hasNext()) {
            val field = iterator.next()
            code.appendLine().append("    ${field.simpleName}·=·${field.simpleName}")
            if (!field.isNullable) {
                code.append("!!")
            }
            if (iterator.hasNext()) {
                code.append(",")
            } else {
                code.appendLine()
            }
        }
        code.append(")").appendLine()

        return FunSpec.builder("build")
            .returns(targetClass)
            .addCode(code.toString())
            .build()
    }


    /** Creates a function that will invoke [check] to confirm that each required field is populated. */
    private fun createCheckRequiredFieldsFunction(): FunSpec {
        val code = StringBuilder()
        info.constructor.parameters
            .filterNot { it.hasDefault || it.isNullable }
            .forEach { field ->
                code.append("check(${field.simpleName}·!=·null) { \"${field.simpleName}·must·not·be·null\" }")
                    .appendLine()
            }

        return FunSpec.builder(CHECK_REQUIRED_FIELDS_FUNCTION_NAME)
            .addCode(code.toString())
            .addModifiers(KModifier.PRIVATE)
            .build()
    }

    private fun KSValueParameter.defaultValue(): String =
        if (this.hasDefault && this.location is FileLocation) {
            parseDefaultValue((this.location as FileLocation).lineNumber)
        } else {
            "null"
        }

    /** Parses the default value for this parameter from source code. */
    private fun KSValueParameter.parseDefaultValue(lineNumber: Int): String {
        val declaringLine = sourceFile[lineNumber - 1]
        val paramIndex = declaringLine.indexOf(this.name!!.getShortName())
        if (paramIndex < 0) {
            logger.warn("Unable to extract default value for ${info.fullyQualifiedTargetName()}.${this} - no declaration found at line $lineNumber")
            return "null"
        }

        var lineIndex = lineNumber - 1
        var equalsIndex = declaringLine.indexOf("=", paramIndex)

        if (equalsIndex < 0) { // we need to move to the next line
            if (sourceFile.size <= lineNumber || sourceFile[lineNumber].indexOf("=") < 0) {
                logger.warn("Unable to extract default value for ${info.fullyQualifiedTargetName()}.${this} - no equals sign found at line $lineNumber")
                return "null"
            }
            lineIndex = lineNumber
            equalsIndex = sourceFile[lineNumber].indexOf("=")
        }

        var inDoubleQuote = false
        var inSingleQuote = false
        var inComment = false
        var brackets = 0
        var index = equalsIndex + 1
        val defaultValue = StringBuilder()
        while (lineIndex < sourceFile.size) {
            val currentLine = sourceFile[lineIndex]

            // helper function to determine whether the current character is escaped (preceded by \)
            fun Char.isUnescaped(cType: Char): Boolean = this == cType && currentLine[index - 1] != '\\'

            val c = currentLine[index]
            if (!inDoubleQuote && !inSingleQuote) {
                if (c == ',' && brackets == 0) { // the comma before the next parameter
                    return defaultValue.toString().trim()
                }
                if (c == ')' && brackets == 0) { // the end bracket of the parent constructor
                    return defaultValue.toString().trim()
                }
                if (c.isOpenBracket) {
                    brackets++
                }
                if (c.isCloseBracket) {
                    brackets--
                }
                if ((c == '/' || c == '*') && currentLine[index - 1] == '/') { // the start of a comment: // or /*
                    inComment = true
                    defaultValue.setLength(defaultValue.length - 1) // drop the first '/' of the comment
                }
            }
            if (c.isUnescaped('"') && !inSingleQuote) { // start or end of a string
                inDoubleQuote = !inDoubleQuote
            }
            if (c.isUnescaped('\'') && !inDoubleQuote) { // start or end of a character
                inSingleQuote = !inSingleQuote
            }
            if (!inComment) {
                defaultValue.append(c)
                index++
            }
            if (inComment || index >= currentLine.length) { // move to the next line
                defaultValue.append("\n")
                lineIndex++
                inComment = false
                index = 0
            }
        }
        return defaultValue.toString().trim()
    }
}

private fun readFileSourceIfDefaultsPresent(info: BuilderInfo) =
    if (info.hasDefaultValues && info.classDeclaration.containingFile != null) {
        File(info.classDeclaration.containingFile!!.filePath).readLines()
    } else {
        emptyList()
    }

private val BuilderInfo.hasDefaultValues: Boolean get() = constructor.parameters.any { it.hasDefault }

private val Char.isOpenBracket: Boolean get() = this == '(' || this == '{'

private val Char.isCloseBracket: Boolean get() = this == ')' || this == '}'

private val KSValueParameter.simpleName: String get() = this.name!!.getShortName()

private val KSValueParameter.isNullable: Boolean get() = this.type.resolve().isMarkedNullable

private fun KSValueParameter.asKotlinTypeName(): TypeName = this.type.resolve().toTypeName()

private fun KSName?.withPrefix(prefix: String): String {
    val propertyName = this!!.getShortName()
    return if (prefix.isBlank()) {
        propertyName
    } else if (propertyName.length == 1) {
        prefix.trim() + propertyName[0].titlecase()
    } else {
        prefix.trim() + propertyName[0].titlecase() + propertyName.substring(1)
    }
}

