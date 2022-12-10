package com.alibaba.ttl3.kotlin

import com.alibaba.crr.composite.Capture
import com.alibaba.ttl3.transmitter.Transmitter
import com.alibaba.ttl3.transmitter.Transmitter.*

/**
 * Util method for simplifying [Transmitter.capture] and [Transmitter.restore] operations.
 *
 * @param captured captured values from other thread from [Transmitter.capture]
 * @param bizLogic biz logic
 * @param R the return type of biz logic
 * @see [Transmitter.runSupplierWithCaptured]
 * @see [Transmitter.runCallableWithCaptured]
 */
inline fun <R> ttlRun(captured: Capture, bizLogic: () -> R): R {
    val backup = replay(captured)
    return try {
        bizLogic()
    } finally {
        restore(backup)
    }
}

/**
 * Util method for simplifying [Transmitter.clear] and [Transmitter.restore] operations.
 *
 * @param bizLogic biz logic
 * @param R the return type of biz logic
 * @see [Transmitter.runSupplierWithClear]
 * @see [Transmitter.runCallableWithClear]
 */
inline fun <R> ttlRunWithClear(bizLogic: () -> R): R {
    val backup = clear()
    return try {
        bizLogic()
    } finally {
        restore(backup)
    }
}
