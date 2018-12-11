package com.alibaba.ttl

import kotlin.reflect.KProperty

/**
 * A shortcut method to instantiate [TransmittableThreadLocal].
 *
 * @since 2.11.0
 */
fun <T> ttl() = TransmittableThreadLocal<T?>()

/**
 * Kotlin Delegate for immutable/mutable values or properties.
 *
 * @param backingProperty is [InheritableThreadLocal] if provided
 *                        or [TransmittableThreadLocal] by default
 *
 * @since 2.11.0
 */
class TtlDelegate<T>(private val backingProperty: InheritableThreadLocal<T?> = ttl()) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = backingProperty.get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        backingProperty.set(value)
    }
}
