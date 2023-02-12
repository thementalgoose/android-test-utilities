package tmg.testutils.junit

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

inline fun <reified T: Any> String.toSealedClass(clazz: KClass<T>): T {
    val model = clazz.sealedSubclasses.firstOrNull { it.simpleName == this }!!
    return when {
        model.objectInstance != null -> model.objectInstance!!
        else -> model.primaryConstructor!!.create() as T
    }
}

fun KFunction<Any>.create() =
    if (parameters.isEmpty()) call()
    else call(*(parameters.map { it.createArgument() }.toTypedArray()))

// grab known types for constructor arguments
fun KParameter.createArgument(): Any? {
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