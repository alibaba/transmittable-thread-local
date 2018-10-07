@file:JvmName("Utils")

package com.alibaba

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.agent.TtlAgent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor


/**
 * Expand thread pool, so as to pre-create and cache threads.
 */
fun expandThreadPool(executor: ExecutorService) {
    val count = if (executor is ThreadPoolExecutor) {
        Math.min(10, executor.maximumPoolSize)
    } else 10

    (0 until count).map {
        executor.submit { sleep(100) }
    }.forEach { it.get() }
}

fun printHead(title: String) {
    println("======================================\n$title\n======================================")
}

////////////////////////////////////////////////////////////////////////////////
// TTL Instances
////////////////////////////////////////////////////////////////////////////////

internal const val PARENT_CREATE_MODIFIED_IN_CHILD = "parent-create-modified-in-child"
internal const val PARENT_CREATE_UNMODIFIED_IN_CHILD = "parent-create-unmodified-in-child"
internal const val PARENT_CREATE_AFTER_CREATE_CHILD = "parent-create-after-create-child"
internal const val CHILD_CREATE = "child-create"

typealias  TtlInstances<T> = ConcurrentMap<String, TransmittableThreadLocal<T>>

fun createParentTtlInstances(ttlInstances: TtlInstances<String> = ConcurrentHashMap()): TtlInstances<String> {
    printTtlInstances(ttlInstances, "Before run(createParentTtlInstances):")

    listOf(PARENT_CREATE_UNMODIFIED_IN_CHILD, PARENT_CREATE_MODIFIED_IN_CHILD).forEach {
        newTtlInstanceAndPut(it, ttlInstances)
    }

    printTtlInstances(ttlInstances, "After run(createParentTtlInstances):")

    return ttlInstances
}

fun createParentTtlInstancesAfterCreateChild(ttlInstances: TtlInstances<String>) {
    printTtlInstances(ttlInstances, "Before run(createParentTtlInstancesAfterCreateChild):")

    newTtlInstanceAndPut(PARENT_CREATE_AFTER_CREATE_CHILD, ttlInstances)

    printTtlInstances(ttlInstances, "After run(createParentTtlInstancesAfterCreateChild):")
}

fun createChildTtlInstancesAndModifyParentTtlInstances(tag: String, ttlInstances: TtlInstances<String>): TtlValues<String> {
    printTtlInstances(ttlInstances, "$tag Before run(createChildTtlInstancesAndModifyParentTtlInstances):")

    // 1. Add new
    val newChildKey = "$CHILD_CREATE$tag"
    newTtlInstanceAndPut(newChildKey, ttlInstances)

    // 2. modify the parent key
    val ttl: TransmittableThreadLocal<String>? = ttlInstances[PARENT_CREATE_MODIFIED_IN_CHILD]
    ttl!!.set("${ttl.get()}$tag")

    printTtlInstances(ttlInstances, "$tag After run(createChildTtlInstancesAndModifyParentTtlInstances):")

    return copyTtlValues(ttlInstances)
}

fun modifyParentTtlInstances(tag: String, ttlInstances: TtlInstances<String>): TtlValues<String> {
    printTtlInstances(ttlInstances, "$tag Before Run(modifyParentTtlInstances):")

    // modify the parent key
    val ttl: TransmittableThreadLocal<String>? = ttlInstances[PARENT_CREATE_MODIFIED_IN_CHILD]
    ttl!!.set("${ttl.get()}$tag")

    printTtlInstances(ttlInstances, "$tag After Run(modifyParentTtlInstances):")

    return copyTtlValues(ttlInstances)
}

fun newTtlInstanceAndPut(key: String, ttlInstances: TtlInstances<String>): TransmittableThreadLocal<String> {
    val ttl = object : TransmittableThreadLocal<String>() {
        override fun toString(): String {
            return "${super.toString()}(${get()})"
        }
    }
    ttl.set(key)

    val old = ttlInstances.putIfAbsent(key, ttl)
    assertNull("Already contains key $key", old)

    return ttl
}

fun <T> printTtlInstances(ttlInstances: TtlInstances<T>, title: String = "") {
    val headList =
            if (title.isBlank()) emptyList()
            else listOf("## $title [${Thread.currentThread().name}] ##")
    val valueString = ttlInstances.filterValues { it.get() != null }
            .map { (k, v) -> "$k: ${v.get()}" }
            .joinToString()
    val output = (headList + valueString + "^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^")
            .filter { it.isNotBlank() }
            .joinToString("\n")
    println(output)
}


////////////////////////////////////////////////////////////////////////////////
// TTL Values
////////////////////////////////////////////////////////////////////////////////

typealias  TtlValues<T> = Map<String, T>

fun <T> copyTtlValues(ttlInstances: TtlInstances<T>): TtlValues<T> =
        ttlInstances.filterValues { it.get() != null }
                .mapValues { (_, v) -> v.get() }

fun <T> assertTtlValues(expected: TtlValues<T>, copied: TtlValues<T>) {
    val message = "Assert Fail:\nexpected: $expected\ncopyTtlValues: $copied"
    assertEquals("size not equals! $message", expected.size, copied.size)
    assertEquals(message, expected, copied)
}

fun <T> assertChildTtlValues(tag: String, values: TtlValues<T>) {
    assertTtlValues(
            mapOf(PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD + tag,
                    PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
                    CHILD_CREATE + tag to CHILD_CREATE + tag),
            values
    )
}

fun <T> assertChildTtlValuesWithParentCreateAfterCreateChild(tag: String, values: TtlValues<T>) {
    assertTtlValues(
            mapOf(PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD + tag,
                    PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
                    CHILD_CREATE + tag to CHILD_CREATE + tag,
                    PARENT_CREATE_AFTER_CREATE_CHILD to PARENT_CREATE_AFTER_CREATE_CHILD),
            values
    )
}

fun <T> assertParentTtlValues(values: TtlValues<T>) {
    assertTtlValues(
            mapOf(PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD, // restored after call!
                    PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
                    PARENT_CREATE_AFTER_CREATE_CHILD to PARENT_CREATE_AFTER_CREATE_CHILD),
            values
    )
}

fun noTtlAgentRun(): Boolean = TtlAgent.isTtlAgentLoaded().let {
    val isUnderAgentSet = System.getProperties().containsKey("run-ttl-test-under-agent")
    assertEquals(isUnderAgentSet, it)
    !it
}
