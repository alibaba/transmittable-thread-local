package com.alibaba.demo.coroutine.ttl_intergration

import com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*
import com.alibaba.ttl.threadpool.agent.TtlAgent
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @see [kotlinx.coroutines.asContextElement]
 */
fun ttlContext(): CoroutineContext =
//        if (TtlAgent.isTtlAgentLoaded()) // FIXME Open the if when implement TtlAgent for koroutine
//            EmptyCoroutineContext
//        else
            TtlElement()

/**
 * @see [kotlinx.coroutines.internal.ThreadLocalElement]
 */
internal class TtlElement : ThreadContextElement<Any> {
    companion object Key : CoroutineContext.Key<TtlElement>

    override val key: CoroutineContext.Key<*> get() = Key

    private var captured: Any =
            capture()

    override fun updateThreadContext(context: CoroutineContext): Any =
            replay(captured)

    override fun restoreThreadContext(context: CoroutineContext, oldState: Any) {
        captured = capture() // FIXME This capture operation is a MUST, WHY? This operation is too expensive?!
        restore(oldState)
    }

    // this method is overridden to perform value comparison (==) on key
    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext =
            if (Key == key) EmptyCoroutineContext else this

    // this method is overridden to perform value comparison (==) on key
    override operator fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? =
            @Suppress("UNCHECKED_CAST")
            if (Key == key) this as E else null
}
