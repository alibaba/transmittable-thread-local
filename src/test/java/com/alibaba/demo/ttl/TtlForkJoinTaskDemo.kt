package com.alibaba.demo.ttl

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRecursiveTask
import java.util.concurrent.ForkJoinPool


val context = TransmittableThreadLocal<String>().apply {
    set("value-set-in-parent")
    println("[parent thread] set ${get()} @ thread ${Thread.currentThread().name}")
}

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    val pool = ForkJoinPool.commonPool()

    val result = pool.invoke(SumTask(1..1000))

    println("[parent thread] computed result: $result @ thread ${Thread.currentThread().name}") // result is 500500
}

private class SumTask(private val numbers: IntRange, private val forkLevel: Int = 0) : TtlRecursiveTask<Int>() {
    override fun compute(): Int =
        if (numbers.count() <= 16) {
            println(String.format("direct compute %9s[%4s] with context ${context.get()} at fork level %2s @ thread ${Thread.currentThread().name}",
                numbers, numbers.count(), forkLevel))

            // compute directly
            numbers.sum()
        } else {
            println(String.format("fork   compute %9s[%4s] with context ${context.get()} at fork level %2s @ thread ${Thread.currentThread().name}",
                numbers, numbers.count(), forkLevel))

            // split task
            val middle = numbers.first + numbers.count() / 2
            val nextForkLevel = forkLevel + 1
            val taskLeft = SumTask(numbers.first until middle, nextForkLevel)
            val taskRight = SumTask(middle..numbers.last, nextForkLevel)

            // fork-join compute
            taskLeft.fork()
            taskRight.fork()
            taskLeft.join() + taskRight.join()
        }
}

/*
Output:

[parent thread] set value-set-in-parent @ thread main
fork   compute   1..1000[1000] with context value-set-in-parent at fork level  0 @ thread main
fork   compute 501..1000[ 500] with context value-set-in-parent at fork level  1 @ thread ForkJoinPool.commonPool-worker-5
fork   compute    1..500[ 500] with context value-set-in-parent at fork level  1 @ thread ForkJoinPool.commonPool-worker-19
fork   compute    1..250[ 250] with context value-set-in-parent at fork level  2 @ thread ForkJoinPool.commonPool-worker-23
fork   compute  251..500[ 250] with context value-set-in-parent at fork level  2 @ thread ForkJoinPool.commonPool-worker-9
fork   compute 751..1000[ 250] with context value-set-in-parent at fork level  2 @ thread ForkJoinPool.commonPool-worker-27
fork   compute  501..750[ 250] with context value-set-in-parent at fork level  2 @ thread ForkJoinPool.commonPool-worker-13
fork   compute  126..250[ 125] with context value-set-in-parent at fork level  3 @ thread ForkJoinPool.commonPool-worker-31
fork   compute  251..375[ 125] with context value-set-in-parent at fork level  3 @ thread ForkJoinPool.commonPool-worker-9
fork   compute 876..1000[ 125] with context value-set-in-parent at fork level  3 @ thread ForkJoinPool.commonPool-worker-7
fork   compute    1..125[ 125] with context value-set-in-parent at fork level  3 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  751..875[ 125] with context value-set-in-parent at fork level  3 @ thread ForkJoinPool.commonPool-worker-21
fork   compute  376..500[ 125] with context value-set-in-parent at fork level  3 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  126..187[  62] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-31
fork   compute  251..312[  62] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-9
fork   compute     1..62[  62] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  501..625[ 125] with context value-set-in-parent at fork level  3 @ thread ForkJoinPool.commonPool-worker-13
fork   compute  751..812[  62] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-21
fork   compute  876..937[  62] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-7
fork   compute  376..437[  62] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  126..156[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-31
fork   compute     1..31[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  251..281[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-9
fork   compute  751..781[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-21
fork   compute  501..562[  62] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-13
fork   compute  376..406[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-3
direct compute     1..15[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  876..906[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-7
direct compute  251..265[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute  126..140[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-31
direct compute  751..765[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
fork   compute  501..531[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-13
direct compute  376..390[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
direct compute  266..281[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute  141..156[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-31
direct compute    16..31[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
direct compute  876..890[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-7
direct compute  501..515[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-13
direct compute  766..781[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
fork   compute  282..312[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-9
direct compute  391..406[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
direct compute  891..906[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-7
fork   compute    32..62[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  157..187[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-31
fork   compute  782..812[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-21
direct compute  516..531[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-13
direct compute  282..296[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute    32..46[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  407..437[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  532..562[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-13
direct compute  157..171[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-31
fork   compute  907..937[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-7
direct compute    47..62[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
direct compute  532..546[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-13
direct compute  297..312[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute  782..796[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
direct compute  907..921[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-7
direct compute  172..187[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-31
direct compute  407..421[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  313..375[  63] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-9
direct compute  797..812[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
direct compute  547..562[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-13
fork   compute   63..125[  63] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-17
direct compute  422..437[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  188..250[  63] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-31
direct compute  922..937[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-7
fork   compute  563..625[  63] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-13
fork   compute  813..875[  63] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-21
fork   compute  313..343[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-9
fork   compute  438..500[  63] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-3
fork   compute    63..93[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  563..593[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-13
direct compute  313..327[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute    63..77[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
fork   compute 938..1000[  63] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-7
fork   compute  188..218[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-31
direct compute  328..343[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute    78..93[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
direct compute  563..577[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-13
fork   compute  438..468[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  813..843[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-21
fork   compute   94..125[  32] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  344..375[  32] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-9
direct compute  188..202[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-31
direct compute  438..452[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  938..968[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-7
direct compute   94..109[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
direct compute  813..827[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
direct compute  578..593[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-13
direct compute  110..125[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
direct compute  938..952[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-7
direct compute  453..468[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
direct compute  203..218[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-31
direct compute  344..359[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
fork   compute  594..625[  32] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-13
direct compute  828..843[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
fork   compute  469..500[  32] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  219..250[  32] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-17
direct compute  953..968[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-7
fork   compute  844..875[  32] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-21
direct compute  594..609[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-13
direct compute  360..375[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute  219..234[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
direct compute  469..484[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
direct compute  610..625[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-13
direct compute  844..859[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
fork   compute 969..1000[  32] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-7
direct compute  485..500[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute  235..250[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
fork   compute  626..750[ 125] with context value-set-in-parent at fork level  3 @ thread ForkJoinPool.commonPool-worker-13
direct compute  860..875[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
direct compute  969..984[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-7
direct compute 985..1000[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
fork   compute  688..750[  63] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-9
fork   compute  626..687[  62] with context value-set-in-parent at fork level  4 @ thread ForkJoinPool.commonPool-worker-31
fork   compute  688..718[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-9
fork   compute  719..750[  32] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-7
fork   compute  657..687[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-27
fork   compute  626..656[  31] with context value-set-in-parent at fork level  5 @ thread ForkJoinPool.commonPool-worker-3
direct compute  688..702[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-9
direct compute  719..734[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-19
direct compute  703..718[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-21
direct compute  626..640[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-3
direct compute  672..687[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-17
direct compute  657..671[  15] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-27
direct compute  735..750[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-23
direct compute  641..656[  16] with context value-set-in-parent at fork level  6 @ thread ForkJoinPool.commonPool-worker-19
[parent thread] computed result: 500500 @ thread main

 */
