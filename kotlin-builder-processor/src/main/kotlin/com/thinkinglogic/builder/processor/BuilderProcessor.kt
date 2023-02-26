package com.thinkinglogic.builder.processor

import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.*
import com.thinkinglogic.builder.annotation.Builder
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import java.io.OutputStream

/**
 * KSP processor for the @Builder annotation.
 * Constructs a Builder for each annotated class/constructor.
 * @see <a href="https://kotlinlang.org/docs/ksp-overview.html">ksp-overview</a>.
 */
class BuilderProcessor(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>,
    kspLogger: KSPLogger,
    private val logger: Logger = Logger(kspLogger, options)
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbolsWithAnnotation = resolver.getSymbolsWithAnnotation(Builder::class.java.name)
        logger.info("${this.javaClass.canonicalName} invoked for ${symbolsWithAnnotation.toList().size} annotations, with options: $options")
        val builderMap = getBuilderInfo(symbolsWithAnnotation)
        builderMap.values.forEach { info ->
            logger.debug( "Creating builder for ${info.fullyQualifiedTargetName()}")
            codeGenerator.createNewFile(info).use { stream ->
                BuilderWriter(info, logger).write(stream)
            }
        }
        val invalidSymbols = symbolsWithAnnotation.filter { !it.validate() }.toList()
        if (invalidSymbols.isNotEmpty()) {
            logger.warn("${invalidSymbols.size} invalid symbols after creating builders: $invalidSymbols")
        }
        return emptyList() // There's no point deferring invalid symbols to the next round, we wouldn't be able to do anything different to this round
    }

    private fun getBuilderInfo(symbolsWithAnnotation: Sequence<KSAnnotated>): Map<String, BuilderInfo> {
        val builderMap = mutableMapOf<String, BuilderInfo>()
        symbolsWithAnnotation.forEach { symbol ->
            var builderInfo = BuilderInfo(symbol, options)
            val name = builderInfo.fullyQualifiedTargetName()
            if (builderMap.containsKey(name)) { // both class and constructor have been annotated, or multiple constructors
                builderInfo = if (builderMap[name]!!.countParams() < builderInfo.countParams()) {
                    builderInfo
                } else {
                    builderMap[name]!!
                }
                logger.warn("Found multiple builder annotations for $name - using the constructor with the most parameters: ${builderInfo.constructor.parameters}")
            }
            builderMap[name] = builderInfo
        }
        return builderMap.toMap()
    }

}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.options, environment.logger)
    }
}

class Logger(
    private val logger: KSPLogger,
    options: Map<String, String>,
    private val isDebugging: Boolean = options["builder.debug"] contentEquals  "true"
) : KSPLogger by logger {
    fun debug(message: String, symbol: KSNode? = null) {
        if (isDebugging) {
            logger.info(message, symbol)
        }
    }
}

fun BuilderInfo.countParams() = this.constructor.parameters.size

fun CodeGenerator.createNewFile(info: BuilderInfo): OutputStream =
    createNewFile(
        Dependencies(true, info.classDeclaration.containingFile!!),
        info.packageName(),
        info.builderName
    )
