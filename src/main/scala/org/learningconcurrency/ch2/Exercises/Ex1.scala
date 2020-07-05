package org.learningconcurrency
package ch2
package Exercises

object Ex1 {
      def parallel [A, B] (a: => A, b: => B): (A, B) = {
        var a1: Option[A] = None
        var b1: Option[B] = None

        val t1 = thread { a1 = Some(a) }
        val t2 = thread { b1 = Some(b) }
        t1.join
        t2.join
        
        (a1.get, b1.get)
    }
}
