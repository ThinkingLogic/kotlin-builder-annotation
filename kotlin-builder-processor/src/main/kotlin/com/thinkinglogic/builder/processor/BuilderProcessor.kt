package com.thinkinglogic.builder.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.DefaultValue
import com.thinkinglogic.builder.annotation.Mutable
import com.thinkinglogic.builder.annotation.NullableType
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.*
import java.util.stream.Collectors
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter.*
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.NOTE

/**
 * Kapt processor for the @Builder annotation.
 * Constructs a Builder for the annotated class.
 */
@SupportedAnnotationTypes("com.thinkinglogic.builder.annotation.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(BuilderProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class BuilderProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val CHECK_REQUIRED_FIELDS_FUNCTION_NAME = "checkRequiredFields"
        val MUTABLE_COLLECTIONS = mapOf(
                List::class.asClassName() to ClassName("kotlin.collections", "MutableList"),
                Set::class.asClassName() to ClassName("kotlin.collections", "MutableSet"),
                Collection::class.asClassName() to ClassName("kotlin.collections", "MutableCollection"),
                Map::class.asClassName() to ClassName("kotlin.collections", "MutableMap"),
                Iterator::class.asClassName() to ClassName("kotlin.collections", "MutableIterator"))
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(Builder::class.java)
        if (annotatedElements.isEmpty()) {
            processingEnv.noteMessage { "No classes annotated with @${Builder::class.java.simpleName} in this round ($roundEnv)" }
            return false
        }

        val generatedSourcesRoot = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.errorMessage { "Can't find the target directory for generated Kotlin files." }
            return false
        }

        processingEnv.noteMessage { "Generating Builders for ${annotatedElements.size} classes in $generatedSourcesRoot" }

        val sourceRootFile = File(generatedSourcesRoot)
        sourceRootFile.mkdir()

        annotatedElements.forEach { annotatedElement ->
            when (annotatedElement.kind) {
                ElementKind.CLASS -> writeBuilderForClass(annotatedElement as TypeElement, sourceRootFile)
                ElementKind.CONSTRUCTOR -> writeBuilderForConstructor(annotatedElement as ExecutableElement, sourceRootFile)
                else -> annotatedElement.errorMessage { "Invalid element type, expected a class or constructor" }
            }
        }

        return false
    }

    /** Invokes [writeBuilder] to create a builder for the given [classElement]. */
    private fun writeBuilderForClass(classElement: TypeElement, sourceRootFile: File) {
        writeBuilder(classElement, classElement.fieldsForBuilder(), sourceRootFile)
    }

    /** Invokes [writeBuilder] to create a builder for the given [constructor]. */
    private fun writeBuilderForConstructor(constructor: ExecutableElement, sourceRootFile: File) {
        writeBuilder(constructor.enclosingElement as TypeElement, constructor.parameters, sourceRootFile)
    }

    /** Writes the source code to create a builder for [classToBuild] within the [sourceRoot] directory. */
    private fun writeBuilder(classToBuild: TypeElement, fields: List<VariableElement>, sourceRoot: File) {
        val packageName = processingEnv.elementUtils.getPackageOf(classToBuild).toString()
        val builderClassName = "${classToBuild.simpleName}Builder"

        processingEnv.noteMessage { "Writing $packageName.$builderClassName" }

        val builderSpec = TypeSpec.classBuilder(builderClassName)
        val builderClass = ClassName(packageName, builderClassName)

        fields.forEach { field ->
            processingEnv.noteMessage { "Adding field: $field" }
            builderSpec.addProperty(field.asProperty())
            builderSpec.addFunction(field.asSetterFunctionReturning(builderClass))
        }

        builderSpec.primaryConstructor(FunSpec.constructorBuilder().build())
        builderSpec.addFunction(createConstructor(fields, classToBuild))
        builderSpec.addFunction(createBuildFunction(fields, classToBuild))
        builderSpec.addFunction(createCheckRequiredFieldsFunction(fields))

        FileSpec.builder(packageName, builderClassName)
                .addType(builderSpec.build())
                .build()
                .writeTo(sourceRoot)
    }

    /** Returns all fields in this type that also appear as a constructor parameter. */
    private fun TypeElement.fieldsForBuilder(): List<VariableElement> {
        val allMembers = processingEnv.elementUtils.getAllMembers(this)
        val fields = fieldsIn(allMembers)
        val constructors = constructorsIn(allMembers)
        val constructorParamNames = constructors
                .flatMap { it.parameters }
                .map { it.simpleName.toString() }
                .toSet()
        return fields.filter { constructorParamNames.contains(it.simpleName.toString()) }
    }

    /** Creates a constructor for [classType] that accepts an instance of the class to build, from which default values are obtained. */
    private fun createConstructor(fields: List<Element>, classType: TypeElement): FunSpec {
        val source = "source"
        val sourceParameter = ParameterSpec.builder(source, classType.asKotlinTypeName()).build()
        val getterFieldNames = classType.getterFieldNames()
        val code = StringBuilder()
        fields.forEach { field ->
            if (getterFieldNames.contains(field.simpleName.toString())) {
                code.append("    this.${field.simpleName}·=·$source.${field.simpleName}")
                        .appendLine()
            }
        }
        return FunSpec.constructorBuilder()
                .addParameter(sourceParameter)
                .callThisConstructor()
                .addCode(code.toString())
                .build()
    }

    /** Returns a set of the names of fields with getters (actually the names of getter methods with 'get' removed and decapitalised). */
    private fun TypeElement.getterFieldNames(): Set<String> {
        val allMembers = processingEnv.elementUtils.getAllMembers(this)
        return methodsIn(allMembers)
                .filter { it.simpleName.startsWith("get") && it.parameters.isEmpty() }
                .map {
                  it.simpleName.toString().substringAfter("get").replaceFirstChar { firstChar -> firstChar.lowercase() }
                }
                .toSet()
    }

    /** Creates a 'build()' function that will invoke a constructor for [returnType], passing [fields] as arguments and returning the new instance. */
    private fun createBuildFunction(fields: List<Element>, returnType: TypeElement): FunSpec {
        val code = StringBuilder("$CHECK_REQUIRED_FIELDS_FUNCTION_NAME()")
        code.appendLine().append("return·${returnType.simpleName}(")
        val iterator = fields.listIterator()
        while (iterator.hasNext()) {
            val field = iterator.next()
            code.appendLine().append("    ${field.simpleName}·=·${field.simpleName}")
            if (!field.isNullable()) {
                code.append("!!")
            }
            if (iterator.hasNext()) {
                code.append(",")
            }
        }
        code.appendLine().append(")").appendLine()

        return FunSpec.builder("build")
                .returns(returnType.asClassName())
                .addCode(code.toString())
                .build()
    }

    /** Creates a function that will invoke [check] to confirm that each required field is populated. */
    private fun createCheckRequiredFieldsFunction(fields: List<Element>): FunSpec {
        val code = StringBuilder()
        fields.filterNot { it.isNullable() }
                .forEach { field ->
                    code.append("    check(${field.simpleName}·!=·null, {\"${field.simpleName}·must·not·be·null\"})")
                      .appendLine()
                }

        return FunSpec.builder(CHECK_REQUIRED_FIELDS_FUNCTION_NAME)
                .addCode(code.toString())
                .addModifiers(KModifier.PRIVATE)
                .build()
    }

    /** Creates a property for the field identified by this element. */
    private fun Element.asProperty(): PropertySpec =
            PropertySpec.builder(simpleName.toString(), asKotlinTypeName().copy(nullable = true), KModifier.PRIVATE)
                    .mutable()
                    .initializer(defaultValue())
                    .build()

    /** Returns the correct default value for this element - the value of any [DefaultValue] annotation, or "null". */
    private fun Element.defaultValue(): String {
        return if (hasAnnotation(DefaultValue::class.java)) {
            val default = this.findAnnotation(DefaultValue::class.java).value
            // make sure that strings are wrapped in quotes
            return if (asType().toString() == "java.lang.String" && !default.startsWith("\"")) {
                "\"$default\""
            } else {
                default
            }
        } else {
            "null"
        }
    }

    /** Creates a function that sets the property identified by this element, and returns the [builder]. */
    private fun Element.asSetterFunctionReturning(builder: ClassName): FunSpec {
        val fieldType = asKotlinTypeName()
        val parameterClass = if (isNullable()) {
            fieldType.copy(nullable = true)
        } else {
            fieldType
        }
        return FunSpec.builder(simpleName.toString())
                .addParameter(ParameterSpec.builder("value", parameterClass).build())
                .returns(builder)
                .addCode("return apply·{ this.$simpleName·=·value }\n")
                .build()
    }

    /**
     * Converts this element to a [TypeName], ensuring that java types such as [java.lang.String] are converted to their Kotlin equivalent,
     * also converting the TypeName according to any [NullableType] and [Mutable] annotations.
     */
    private fun Element.asKotlinTypeName(): TypeName {
        var typeName = asType().asKotlinTypeName()
        if (typeName is ParameterizedTypeName) {
            if (hasAnnotation(NullableType::class.java)
                    && assert(typeName.typeArguments.isNotEmpty(), "NullableType annotation should not be applied to a property without type arguments!")) {
                typeName = typeName.withNullableType()
            }
            if (hasAnnotation(Mutable::class.java)
                    && assert(MUTABLE_COLLECTIONS.containsKey(typeName.rawType), "Mutable annotation should not be applied to non-mutable collections!")) {
                typeName = typeName.asMutableCollection()
            }
        }
        return typeName
    }

    /**
     * Converts this type to one containing nullable elements.
     *
     * For instance `List<String>` is converted to `List<String?>`, `Map<String, String>` to `Map<String, String?>`).
     * @throws NoSuchElementException if [this.typeArguments] is empty.
     */
    private fun ParameterizedTypeName.withNullableType(): ParameterizedTypeName {
        val lastType = this.typeArguments.last().copy(nullable = true)
        val typeArguments = ArrayList<TypeName>()
        typeArguments.addAll(this.typeArguments.dropLast(1))
        typeArguments.add(lastType)
        return this.rawType.parameterizedBy(*typeArguments.toTypedArray())
    }

    /**
     * Converts this type to its mutable equivalent.
     *
     * For instance `List<String>` is converted to `MutableList<String>`.
     * @throws NullPointerException if [this.rawType] cannot be mapped to a mutable collection
     */
    private fun ParameterizedTypeName.asMutableCollection(): ParameterizedTypeName {
        val mutable = MUTABLE_COLLECTIONS[rawType]!!
                .parameterizedBy(*this.typeArguments.toTypedArray())
                .copy(annotations = this.annotations) as ParameterizedTypeName
        return if (isNullable) {
            mutable.copy(nullable = true) as ParameterizedTypeName
        } else {
            mutable
        }
    }

    /** Converts this TypeMirror to a [TypeName], ensuring that java types such as [java.lang.String] are converted to their Kotlin equivalent. */
    private fun TypeMirror.asKotlinTypeName(): TypeName {
        return when (this) {
            is PrimitiveType -> processingEnv.typeUtils.boxedClass(this as PrimitiveType?).asKotlinClassName()
            is ArrayType -> {
                val arrayClass = ClassName("kotlin", "Array")
                return arrayClass.parameterizedBy(this.componentType.asKotlinTypeName())
            }
            is DeclaredType -> {
                val typeName = this.asTypeElement().asKotlinClassName()
                if (this.typeArguments.isNotEmpty()) {
                    val kotlinTypeArguments = typeArguments.stream()
                            .map { it.asKotlinTypeName() }
                            .collect(Collectors.toList())
                            .toTypedArray()
                    return typeName.parameterizedBy(*kotlinTypeArguments)
                }
                return typeName
            }
            else -> this.asTypeElement().asKotlinClassName()
        }
    }

    /** Converts this element to a [ClassName], ensuring that java types such as [java.lang.String] are converted to their Kotlin equivalent. */
    private fun TypeElement.asKotlinClassName(): ClassName {
        val className = asClassName()
        return try {
            // ensure that java.lang.* and java.util.* etc classes are converted to their kotlin equivalents
            Class.forName(className.canonicalName).kotlin.asClassName()
        } catch (e: ClassNotFoundException) {
            // probably part of the same source tree as the annotated class
            className
        }
    }

    /** Returns the [TypeElement] represented by this [TypeMirror]. */
    private fun TypeMirror.asTypeElement() = processingEnv.typeUtils.asElement(this) as TypeElement

    /** Returns true as long as this [Element] is not a [PrimitiveType] and does not have the [NotNull] annotation. */
    private fun Element.isNullable(): Boolean {
        if (this.asType() is PrimitiveType) {
            return false
        }
        return !hasAnnotation(NotNull::class.java)
    }

    /**
     * Returns true if this element has the specified [annotation], or if the parent class has a matching constructor parameter with the annotation.
     * (This is necessary because builder annotations can be applied to both fields and constructor parameters - and constructor parameters take precedence.
     * Rather than require clients to specify, for instance, `@field:NullableType`, this method also checks for annotations of constructor parameters
     * when this element is a field).
     */
    private fun Element.hasAnnotation(annotation: Class<*>): Boolean {
        return hasAnnotationDirectly(annotation) || hasAnnotationViaConstructorParameter(annotation)
    }

    /** Return true if this element has the specified [annotation]. */
    private fun Element.hasAnnotationDirectly(annotation: Class<*>): Boolean {
        return this.annotationMirrors
                .map { it.annotationType.toString() }
                .toSet()
                .contains(annotation.name)
    }

    /** Return true if there is a constructor parameter with the same name as this element that has the specified [annotation]. */
    private fun Element.hasAnnotationViaConstructorParameter(annotation: Class<*>): Boolean {
        val parameterAnnotations = getConstructorParameter()?.annotationMirrors ?: listOf()
        return parameterAnnotations
                .map { it.annotationType.toString() }
                .toSet()
                .contains(annotation.name)
    }

    /** Returns the first constructor parameter with the same name as this element, if any such exists. */
    private fun Element.getConstructorParameter(): VariableElement? {
        val enclosingElement = this.enclosingElement
        return if (enclosingElement is TypeElement) {
            val allMembers = processingEnv.elementUtils.getAllMembers(enclosingElement)
            constructorsIn(allMembers)
                    .flatMap { it.parameters }
                    .firstOrNull { it.simpleName == this.simpleName }
        } else {
            null
        }
    }

    /**
     * Returns the given annotation, retrieved from this element directly, or from the corresponding constructor parameter.
     *
     * @throws NullPointerException if no such annotation can be found - use [hasAnnotation] before calling this method.
     */
    private fun <A : Annotation> Element.findAnnotation(annotation: Class<A>): A {
        return if (hasAnnotationDirectly(annotation)) {
            getAnnotation(annotation)
        } else {
            getConstructorParameter()!!.getAnnotation(annotation)
        }
    }

    /** Returns the given [assertion], logging an error message if it is not true. */
    private fun Element.assert(assertion: Boolean, message: String): Boolean {
        if (!assertion) {
            this.errorMessage { message }
        }
        return assertion
    }

    /** Prints an error message using this element as a position hint. */
    private fun Element.errorMessage(message: () -> String) {
        processingEnv.messager.printMessage(ERROR, message(), this)
    }
}

private fun ProcessingEnvironment.errorMessage(message: () -> String) {
    this.messager.printMessage(ERROR, message())
}

private fun ProcessingEnvironment.noteMessage(message: () -> String) {
    this.messager.printMessage(NOTE, message())
}
