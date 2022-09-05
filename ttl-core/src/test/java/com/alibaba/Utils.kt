package com.alibaba

import com.alibaba.ttl3.TtlCallable
import com.alibaba.ttl3.TtlRunnable
import com.alibaba.ttl3.agent.TtlAgentStatus
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.lang.Thread.sleep
import java.time.Duration
import java.util.concurrent.*

/**
 * Expand thread pool, to pre-create and cache threads.
 */
fun expandThreadPool(executor: ExecutorService) {
    val cpuCountX2 = Runtime.getRuntime().availableProcessors() * 2

    val count = if (executor is ThreadPoolExecutor) {
        (executor.maximumPoolSize * 2).coerceAtMost(cpuCountX2)
    } else cpuCountX2

    (0 until count).map {
        executor.submit { sleep(10) }
    }.forEach { it.getForTest() }
}


////////////////////////////////////////////////////////////////////////////////
// shutdown/await util methods for test
////////////////////////////////////////////////////////////////////////////////

private val timeout = Duration.ofSeconds(3)

fun ExecutorService.shutdownForTest() {
    shutdown()
    withClue("Fail to shutdown thread pool") {
        awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS).shouldBeTrue()
    }
}

fun <T> Future<T>.getForTest(): T = this.get(timeout.toMillis(), TimeUnit.MILLISECONDS)


////////////////////////////////////////////////////////////////////////////////
// TTL Agent
////////////////////////////////////////////////////////////////////////////////

fun hasTtlAgentRun(): Boolean = TtlAgentStatus.getInstance().isTtlAgentLoaded.also {
    val key = "run-ttl-test-under-agent"
    if (it) {
        System.getProperties().containsKey(key).shouldBeTrue()
        System.getProperty(key) shouldBe "true"
    } else {
        System.getProperties().containsKey(key).shouldBeFalse()
    }
}

fun noTtlAgentRun(): Boolean = !hasTtlAgentRun()

fun Runnable.ttlWrapIfNoTtlAgentRun() =
    if (noTtlAgentRun()) TtlRunnable.get(this)!!
    else this

fun <T> Callable<T>.ttlWrapIfNoTtlAgentRun() =
    if (noTtlAgentRun()) TtlCallable.get(this)!!
    else this
