package org.learningconcurrency
package ch3
package Exercises

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

object Ex2 extends App {

    class TreiberStack [A] {

        private val stack = new AtomicReference[List[A]](Nil)

        @tailrec final def push (x: A): Unit = {
            val xs = stack.get
            if(!stack.compareAndSet(xs, x :: xs)) push(x)
        }

        @tailrec final def pop (): Option[A] = stack.get match {
            case lst @ (x :: xs) => 
                if (!stack.compareAndSet(lst, xs)) pop() 
                else Some(x)
            case Nil => None
        }

        def iterator (): Iterator[A] = stack.get.iterator
    }
  


    val st = new TreiberStack[Int]

    val t1 = ch2.thread { for ( x <- 0 to 100) st.push(x) }
    val t2 = ch2.thread { for ( x <- 101 to 200 ) st.push(x) }
    val t3 = ch2.thread { for ( x <- 201 to 300 ) st.push(x) }

    t1.join()
    t2.join()
    t3.join()

    for (x <- st.iterator) println(x)
    
}
