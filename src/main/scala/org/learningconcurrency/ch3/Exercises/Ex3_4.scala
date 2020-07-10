package org.learningconcurrency
package ch3
package Exercises

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec


class ConcurrentSortedList [A] (implicit val ord: Ordering[A]) {

    val list = new AtomicReference[List[A]](Nil)

    @tailrec final def add (x: A): Unit = {
        val xs = list.get
        val nxs = xs.takeWhile(ord.compare(x, _) >= 0) ::: List(x) ::: xs.dropWhile(ord.compare(x, _) >= 0)
        if (!list.compareAndSet(xs, nxs)) add(x)
    }

    def iterator: Iterator[A] = list.get.iterator

}

object Ex3 extends App {

    val lst = new ConcurrentSortedList[Int]()

    val inserters = for { i <- 0 until 5 } yield ch2.thread {
        for ( i <- 1 to 100 ) lst.add((Math.random()*100).toInt)
    }

    Thread.sleep(40)
    println("Consistent Iterator: ")
    for (x <- lst.iterator) {println(x)}

    inserters.foreach(_.join())
    print("Complete")
}
