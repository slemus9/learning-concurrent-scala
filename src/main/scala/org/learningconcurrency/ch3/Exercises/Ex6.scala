package org.learningconcurrency
package ch3
package Exercises

import java.util.concurrent.atomic.AtomicReference


class PureLazyCell [A] (init: => A) {

    val a = new AtomicReference[Option[A]](None)

    def apply (): A = a.get match {
        case Some(value) => value
        case None => {
            val newa = init
            if (!a.compareAndSet(None, Some(newa))) apply()
            else newa
        }
    }
}

class PureLazyCell1 [A] (init: => A) {

    def apply (): A = init
}

object Ex6 extends App {
  
    val init = { "An initialization" }

    val pureLazyCell = new PureLazyCell(init)

    for { i <- 1 to 30 } yield ch2.thread {
        println(s"Lazy value: ${pureLazyCell.apply()}")
    }

}
