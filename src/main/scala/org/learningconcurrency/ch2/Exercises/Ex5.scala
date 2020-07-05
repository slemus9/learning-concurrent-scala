package org.learningconcurrency
package ch2
package Exercises

object Ex5 extends App {

    class SyncVarGuarded [T] {
        private var variable: Option[T] = None

        def getWait (): T = this.synchronized {
            while (variable.isEmpty) this.wait
            val x = variable.get
            variable = None
            this.notify
            x
        }

        def putWait (x: T): Unit = this.synchronized {
            while (variable.nonEmpty) this.wait
            variable = Some(x)
            this.notify
        }
    }
    
    val syncVar = new SyncVarGuarded[Int]

    val prod = thread {
        var i = 0
        while (i < 15) {
            syncVar.putWait(i)
            i += 1
        }
    }

    val cons = thread {
        var i = 0
        while (i < 15) {
            log(syncVar.getWait.toString)
            i += 1
        }
    }

    prod.join
    cons.join
}
