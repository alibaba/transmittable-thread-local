package com.alibaba.perf

import java.util.*

private val random = Random()

internal fun bytes2Hex(bytes: ByteArray): String {
    val sb = StringBuilder(1024)
    for (b in bytes) {
        val s = Integer.toHexString(b.toInt() and 0xFF)
        sb.append(if (s.length == 1) "0$s" else s)
    }
    return sb.toString()
}

internal fun getRandomBytes(): ByteArray {
    val bytes = ByteArray(1024)
    random.nextBytes(bytes)
    return bytes
}

internal fun getRandomString(): String {
    return bytes2Hex(getRandomBytes())
}

