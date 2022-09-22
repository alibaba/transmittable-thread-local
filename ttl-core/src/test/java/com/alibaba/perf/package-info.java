/**
 * Performance test cases.
 * <p>
 * TPS test:
 * <ul>
 *     <li>{@link com.alibaba.perf.tps.CreateTransmittableThreadLocalInstanceTpsKt#main()}</li>
 *     <li>{@link com.alibaba.perf.tps.CreateThreadLocalInstanceTpsKt#main()}</li>
 * </ul>
 * <p>
 * Memory leak test:
 * <ul>
 *    <li>{@link com.alibaba.perf.memoryleak.NoMemoryLeak_TransmittableThreadLocal_NoRemoveKt#main()}</li>
 *    <li>{@link com.alibaba.perf.memoryleak.NoMemoryLeak_ThreadLocal_NoRemoveKt#main()}</li>
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
package com.alibaba.perf;
