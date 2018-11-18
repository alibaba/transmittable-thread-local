import kotlinx.coroutines.*

private val threadLocal = ThreadLocal<String?>() // declare thread-local variable

/**
 * [Thread-local data - Coroutine Context and Dispatchers - Kotlin Programming Language](https://kotlinlang.org/docs/reference/coroutines/coroutine-context-and-dispatchers.html#thread-local-data)
 */
fun main() = runBlocking<Unit> {
    threadLocal.set("main")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
        yield()
        println("After yield, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
    }
    job.join()

    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
}
