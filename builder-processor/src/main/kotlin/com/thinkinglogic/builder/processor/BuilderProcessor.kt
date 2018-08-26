package com.thinkinglogic.builder.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.NullableType
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.stream.Collectors
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter.constructorsIn
import javax.lang.model.util.ElementFilter.fieldsIn
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

        annotatedElements.forEach { annotatedClass ->
            if (annotatedClass !is TypeElement) {
                annotatedClass.errorMessage { "Invalid element type, expected a class" }
                return@forEach
            }

            writeBuilder(annotatedClass, sourceRootFile)
        }

        return false
    }

    /**
     * Writes the source code to create a builder for [classToBuild] within the [sourceRoot] directory.
     */
    private fun writeBuilder(classToBuild: TypeElement, sourceRoot: File) {
        val packageName = processingEnv.elementUtils.getPackageOf(classToBuild).toString()
        val builderClassName = "${classToBuild.simpleName}Builder"

        processingEnv.noteMessage { "Writing $packageName.$builderClassName" }

        val builderSpec = TypeSpec.classBuilder(builderClassName)
        val builderClass = ClassName(packageName, builderClassName)
        val fields = classToBuild.fieldsForBuilder()

        fields.forEach { field ->
            processingEnv.noteMessage { "Adding field: $field" }
            builderSpec.addProperty(field.asProperty())
            builderSpec.addFunction(field.asSetterFunctionReturning(builderClass))
        }

        builderSpec.addFunction(createBuildFunction(fields, classToBuild))
        builderSpec.addFunction(createCheckRequiredFieldsFunction(fields))

        FileSpec.builder(packageName, builderClassName)
                .addType(builderSpec.build())
                .build()
                .writeTo(sourceRoot)
    }

    /**
     * Returns all fields in this type that also appear as a constructor parameter.
     */
    private fun TypeElement.fieldsForBuilder(): List<VariableElement> {
        val allMembers = processingEnv.elementUtils.getAllMembers(this)
        val fields = fieldsIn(allMembers)
        val constructors = constructorsIn(allMembers)
        val constructorParamNames = constructors
                .flatMap { it.parameters }
                .map { it.simpleName.toString()}
                .toSet()
        return fields.filter { constructorParamNames.contains(it.simpleName.toString()) }
    }

    /**
     * Creates a 'build()' function that will invoke a constructor for [returnType], passing [fields] as arguments and returning the new instance.
     */
    private fun createBuildFunction(fields: List<Element>, returnType: TypeElement): FunSpec {
        val code = StringBuilder("$CHECK_REQUIRED_FIELDS_FUNCTION_NAME()")
        code.appendln().append("return ${returnType.simpleName}(")
        val iterator = fields.listIterator()
        while (iterator.hasNext()) {
            val field = iterator.next()
            code.appendln().append("    ${field.simpleName} = ${field.simpleName}")
            if (!field.isNullable()) {
                code.append("!!")
            }
            if (iterator.hasNext()) {
                code.append(",")
            }
        }
        code.appendln().append(")").appendln()

        return FunSpec.builder("build")
                .returns(returnType.asClassName())
                .addCode(code.toString())
                .build()
    }

    /**
     * Creates a function that will invoke [check] to confirm that each required field is populated.
     */
    private fun createCheckRequiredFieldsFunction(fields: List<Element>): FunSpec {
        val code = StringBuilder()
        fields
                .filterNot { it.isNullable() }
                .forEach { field ->
                    code.append("    check(${field.simpleName} != null, {\"${field.simpleName} must not be null\"})").appendln()
                }

        return FunSpec.builder(CHECK_REQUIRED_FIELDS_FUNCTION_NAME)
                .addCode(code.toString())
                .addModifiers(KModifier.PRIVATE)
                .build()
    }

    /**
     * Creates a property for the field identified by this element.
     */
    private fun Element.asProperty(): PropertySpec =
            PropertySpec.varBuilder(simpleName.toString(), asKotlinTypeName().asNullable(), KModifier.PRIVATE)
                    .initializer("null")
                    .build()

    /**
     * Creates a function that sets the property identified by this element, and returns the [builder].
     */
    private fun Element.asSetterFunctionReturning(builder: ClassName): FunSpec {
        val fieldType = asKotlinTypeName()
        val parameterClass = if (isNullable()) {
            fieldType.asNullable()
        } else {
            fieldType
        }
        return FunSpec.builder(simpleName.toString())
                .addParameter(ParameterSpec.builder("value", parameterClass).build())
                .returns(builder)
                .addCode("return apply { $simpleName = value }\n")
                .build()
    }

    /**
     * Converts this element to a [TypeName], ensuring that java types such as [java.lang.String] are converted to their Kotlin equivalent.
     */
    private fun Element.asKotlinTypeName(): TypeName {
        val typeName = asType().asKotlinTypeName()
        if (hasAnnotation(NullableType::class.java) && typeName is ParameterizedTypeName) {
            // for example '@NullableType List<String?>' or '@NullableType Map<String, Long?>'
            if (typeName.typeArguments.isEmpty()) {
                this.errorMessage { "NullableType annotation should not be applied to a property without type arguments!" }
                return typeName
            }
            // mark the element type contained by the collection/map as nullable
            val lastType = typeName.typeArguments.last().asNullable()
            val typeArguments = ArrayList<TypeName>()
            typeArguments.addAll(typeName.typeArguments.dropLast(1))
            typeArguments.add(lastType)
            return typeName.rawType.parameterizedBy(*typeArguments.toTypedArray())
        }
        return typeName
    }

    /**
     * Converts this TypeMirror to a [TypeName], ensuring that java types such as [java.lang.String] are converted to their Kotlin equivalent.
     */
    private fun TypeMirror.asKotlinTypeName(): TypeName {
        return when (this) {
            is PrimitiveType -> processingEnv.typeUtils.boxedClass(this as PrimitiveType?).asKotlinClassName()
            is ArrayType -> {
                val arrayClass = ClassName("kotlin", "Array")
                return arrayClass.parameterizedBy(this.componentType.asKotlinTypeName())
            }
            is DeclaredType -> {
                val typeName = this.asTypeElement().asKotlinClassName()
                if (!this.typeArguments.isEmpty()) {
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

    /**
     * Converts this element to a [ClassName], ensuring that java types such as [java.lang.String] are converted to their Kotlin equivalent.
     */
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

    private fun TypeMirror.asTypeElement() = processingEnv.typeUtils.asElement(this) as TypeElement

    private fun Element.isNullable(): Boolean {
        if (this.asType() is PrimitiveType) {
            return false
        }
        return !hasAnnotation(NotNull::class.java)
    }

    private fun Element.hasAnnotation(annotationClass: Class<*>): Boolean {
        return this.annotationMirrors
                .map { it.annotationType.toString() }
                .toSet()
                .contains(annotationClass.name)
    }

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
