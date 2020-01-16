import kotlinx.coroutines.*

private val threadLocal = ThreadLocal<String?>() // declare thread-local variable

/**
 * [Thread-local data - Coroutine Context and Dispatchers - Kotlin Programming Language](https://kotlinlang.org/docs/reference/coroutines/coroutine-context-and-dispatchers.html#thread-local-data)
 */
fun main() = runBlocking {
    threadLocal.set("main")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

    val block: suspend CoroutineScope.() -> Unit = {
        println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
        yield()
        println("After yield, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
    }

    println()
    launch(block = block).join()

    println()
    launch(threadLocal.asContextElement(value = "launch"), block = block).join()

    println()
    launch(Dispatchers.Default, block = block).join()

    println()
    launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch"), block = block).join()

    println()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
}
