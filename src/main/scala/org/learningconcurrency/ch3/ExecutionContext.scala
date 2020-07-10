package org.learningconcurrency
package ch3

import scala.concurrent
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext

object ExecutorsCreate extends App {
    val executor = new ForkJoinPool
    executor.execute(new Runnable {
        def run () = log("This task is run asynchronously")
    })
    //Thread.sleep(500)
    executor.shutdown()
    executor.awaitTermination(60, TimeUnit.SECONDS)
}

/**
  * The global companion object of Execution context contains 
  * a default implementation using ForkJoinPool
  */
object ExecutionContextGlobal extends App {
    val ectx = ExecutionContext.global
    ectx.execute(new Runnable {
        def run () = log("Running on the execution context.")
    })
    Thread.sleep(500)
}

object ExecutionContextCreate extends App {
    // The ForkJoinPool will usually keep two worker threads in its pool
    val pool = new ForkJoinPool(2)
    val ectx = ExecutionContext.fromExecutorService(pool)
    ectx.execute(new Runnable {
        def run (): Unit = log("Running on the execution context again.")
    })
    Thread.sleep(500)
}

/**
  * One caveat regarding the use of ExecutionContext is that tasks
  * are unable to execute if the threads become unavailable (all of them are
  * running other tasks). Executing blocking operations on ExecutionContext can
  * cause starvation
  */
object ExecutionContextSleep extends App {
    // The sleep function puts all the threads in waiting state
    for (i <- 0 until 32) execute {
        Thread.sleep(2000)
        log(s"Task $i completed")
    }
    Thread.sleep(10000)
}
