package  com.alibaba.demo.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * related material:
 *
 * - [Work with ThreadLocal-sensitive Components #119 - Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines/issues/119)
 *      - [How to use code that relies on ThreadLocal with Kotlin coroutines - Stack Overflow](https://stackoverflow.com/questions/46227462/how-to-use-code-that-relies-on-threadlocal-with-kotlin-coroutines/46227463)
 * - [README.md of Module kotlinx-coroutines-slf4j - Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines/blob/master/integration/kotlinx-coroutines-slf4j/README.md)
 */
fun main(): Unit = runBlocking {
    myThreadLocal.set(MyData("main value"))

    async(Dispatchers.IO) {
        "world(${myThreadLocal.get().data})"
    }.run {
        println("Hello ${await()}!")
    }

    async(MyThreadLocalContextContinuationInterceptor(myThreadLocal.get(), Dispatchers.IO)) {
        "world(${myThreadLocal.get().data})"
    }.run {
        println("Hello ${await()}!")
    }
}

private val myThreadLocal = object : ThreadLocal<MyData>() {
    override fun initialValue(): MyData {
        return MyData("init value")
    }
}

private class MyThreadLocalContextContinuationInterceptor(
        private var myData: MyData,
        private val dispatcher: ContinuationInterceptor
) : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
            dispatcher.interceptContinuation(Wrapper(continuation))

    inner class Wrapper<T>(private val continuation: Continuation<T>) : Continuation<T> {

        private inline fun wrap(block: () -> Unit) {
            try {
                myThreadLocal.set(myData)
                block()
            } finally {
                myData = myThreadLocal.get()
            }
        }

        override val context: CoroutineContext get() = continuation.context

        override fun resumeWith(result: Result<T>) = wrap { continuation.resumeWith(result) }
    }
}

private data class MyData(val data: String)
