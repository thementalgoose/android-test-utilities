package tmg.testutils.junit

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.support.AnnotationConsumer
import org.junit.platform.commons.util.Preconditions
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

// https://github.com/dpreussler/junit5-kotlin
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ArgumentsSource(SealedClassesArgumentsProvider::class)
annotation class SealedClassesSource(
    val factoryClass: KClass<out TypeFactory> = DefaultTypeFactory::class,
    val names: Array<String> = [],
    val mode: Mode = Mode.INCLUDE
) {

    // factory to create actual instances of a class
    interface TypeFactory {
        fun create(what: KClass<*>): Any
    }

    enum class Mode {
        INCLUDE,
        EXCLUDE
    }
}

// default factory that can return singletons and instances of classes with empty constructor
open class DefaultTypeFactory: SealedClassesSource.TypeFactory {

    override fun create(what: KClass<*>): Any {
        return what.objectInstance ?: what.constructors.first().create()
    }

    private fun KFunction<Any>.create() =
        if (parameters.isEmpty()) call()
        else call(*(parameters.map { it.createArgument() }.toTypedArray()))

    // grab known types for constructor arguments
    private fun KParameter.createArgument(): Any? {
        return when(type.classifier) {
            Int::class -> 0
            Byte::class -> 0.toByte()
            Short::class -> 0.toShort()
            String::class -> ""
            Float::class -> 0f
            Long::class -> 0L
            else -> null
        }
    }
}

internal class SealedClassesArgumentsProvider : ArgumentsProvider, AnnotationConsumer<SealedClassesSource> {

    private lateinit var source: SealedClassesSource
    private val factory by lazy { source.factoryClass.java.newInstance() }


    override fun provideArguments(context: ExtensionContext) =
        determineClass(context)
            .sealedSubclasses // children
            .squashed() // flatten the tree
            .filter(source.names, source.mode)
            .map { factory.create(it) } // create instances
            .map { Arguments.of(it) } // convert to junit arguments
            .stream()

    override fun accept(source: SealedClassesSource) {
        this.source = source
    }

    // flatten a tree and keep only leaves
    private fun <T : Any> List<KClass<out T>>.squashed(): List<KClass<out T>> = flatMap {
        (if (it.sealedSubclasses.isEmpty()) listOf(it) else emptyList()) + it.sealedSubclasses.squashed<T>()
    }

    private fun <T : Any> List<KClass<out T>>.filter(
        names: Array<String>,
        mode: SealedClassesSource.Mode
    ): List<KClass<out T>> = filter { clazz ->
        if (names.isNotEmpty()) {
            when (mode) {
                SealedClassesSource.Mode.EXCLUDE -> !names.contains(clazz.simpleName)
                SealedClassesSource.Mode.INCLUDE -> names.contains(clazz.simpleName)
            }
        } else {
            true
        }
    }


    // extracts the class from method parameter, inspired bu org.junit.jupiter.params.provider.EnumArgumentsProvider.determineEnumClass(ExtensionContext)
    private fun determineClass(context: ExtensionContext): KClass<*> {
        val parameterTypes = context.requiredTestMethod.parameterTypes
        Preconditions.condition(parameterTypes.isNotEmpty()) {
            "Test method must declare at least one parameter: ${context.requiredTestMethod.toGenericString()}"
        }
        return parameterTypes[0].kotlin
    }
}