package org.learningconcurrency
package ch2
package Exercises

import org.learningconcurrency.ch2.Exercises.Ex3.SyncVar


object Ex4 extends App {

    val syncVar = new SyncVar[Int]
    val t1 = thread {
        var i = 0
        while (i < 15) {
            if (syncVar.isEmpty) {
                syncVar.put(i)
                i += 1
            }
        }
    }

    val t2 = thread {
        var i = 0
        while (i < 15) {
            if (syncVar.nonEmpty) {
                syncVar.get() match {
                    case Left(err) => log(s"Error: $err")
                    case Right(value) => log(value.toString)
                }
                i += 1
            }
        }
    }

    t1.join()
    t2.join()
}
