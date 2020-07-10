package org.learningconcurrency
package ch3

import scala.collection._
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import java.util.concurrent.LinkedBlockingDeque

object CollectionsBad extends App {
    val buffer = mutable.ArrayBuffer[Int]()
    def asyncAdd (numbers: Seq[Int]) = execute {
        buffer ++= numbers
        log(s"buffer = $buffer")
    }
    asyncAdd(0 until 10)
    asyncAdd(10 until 20)
    Thread.sleep(500)
}

/**
  * Take into account that using synchronized or atomic variables
  * can lead to scalability problems when many threads access an
  * atomic variable at once.
  */
class AtomicBuffer[T] {
    private val buffer = new AtomicReference[List[T]](Nil)

    @tailrec final def += (x: T): Unit = {
        val xs = buffer.get
        val nxs = x :: xs
        if (!buffer.compareAndSet(xs, nxs)) this += x
    }
}

/**
  * In concurrent queues (or any other concurrent
  * structures), the iterators are not consistent.
  * This is not true only for the CopyOnWriteArrayList and
  * CopyOnWriteArraySet structures
  */
object CollectionsIterators extends App {
    val queue = new LinkedBlockingDeque[String]
    for (i <- 1 to 5500) queue.offer(i.toString)

    execute {
        val it = queue.iterator
        while (it.hasNext) log(it.next)
    }

    for (i <- 1 to 5500) queue.poll()
    Thread.sleep(1000)
}

