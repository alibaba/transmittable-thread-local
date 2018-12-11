package com.alibaba.ttl

import kotlin.reflect.KProperty

/**
 * @since 2.11.0
 */
fun <T> ttl() = TransmittableThreadLocal<T?>()

/**
 * @since 2.11.0
 */
class TtlDelegate<T> {
    private var _value = ttl<T>()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = _value.get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        _value.set(value)
    }
}
