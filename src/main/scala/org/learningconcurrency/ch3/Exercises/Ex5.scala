package org.learningconcurrency
package ch3
package Exercises

//case class LazyVal [A] (val value: A)

class LazyCell [A] (init: => A) {

    @volatile
    var a: Option[A] = None

    def apply (): A = this.synchronized { a match {
        case None => {
            val value = init
            a = Some(value)
            value
        }
        case Some(value) => value
    }
    }
}

object  Ex5 extends App {

    def init = { "An initialization" }

    val lazyCell = new LazyCell (init)

    for { i <- 1 to 30 } yield ch2.thread {
        println(s"Lazy value: ${lazyCell.apply()}")
    }
}
