package org.learningconcurrency
package ch2
package Exercises

object Ex2 {
    def periodically (duration: Long) (b: => Unit): Unit = {
        val t = thread {
            while (true) {
                b
                Thread.sleep(duration)
            }
        }
    }
}
