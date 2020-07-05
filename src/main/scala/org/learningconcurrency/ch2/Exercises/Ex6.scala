package org.learningconcurrency.ch2.Exercises

import scala.collection.mutable

object Ex6 {

    class SyncQueue [T] (n: Int) {

        private val queue = mutable.Queue[T]()

        def get (): T = {
            while (queue.isEmpty) this.wait
            val x = queue.dequeue
            this.notify
            x
        }

        def put (x: T): Unit = {
            while (queue.size == n) this.wait
            queue += x
            this.notify
        }
    }


}
