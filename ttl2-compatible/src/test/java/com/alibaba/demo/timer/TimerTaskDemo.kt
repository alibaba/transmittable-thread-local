package com.alibaba.demo.timer

import java.text.SimpleDateFormat
import java.util.*

/**
 * @see [Java Timer TimerTask Example](https://www.journaldev.com/1050/java-timer-timertask-example)
 */
fun main() {
    val timerTask = MyTimerTask()

    // running timer task as daemon thread
    val timer = Timer(true)
    timer.scheduleAtFixedRate(timerTask, 0, 300)
    println("TimerTask scheduled")

    // cancel after sometime
    Thread.sleep(1_000)
    timer.cancel()
    println("TimerTask cancelled")

    Thread.sleep(300)
}

class MyTimerTask : TimerTask() {
    override fun run() {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        println("Timer task started at: ${format.format(Date())}")
        Thread.sleep(200)
        println("Timer task finished at: ${format.format(Date())}")
    }

}
