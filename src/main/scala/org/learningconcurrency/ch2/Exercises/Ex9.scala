package org.learningconcurrency.ch2.Exercises

import scala.collection.mutable
import org.learningconcurrency.ch2.ThreadsProtectedUid.getUniqueId

object Ex9 {

    case class Task (val priority: Int, body: () => Unit) extends Ordered[Task] {

        def compare (that: Task) = that.priority - this.priority
    }

    class PriorityTaskPool (val p: Int) {

        private val queue = mutable.PriorityQueue[Task]()

        private val workers = for { i <- 1 to p } yield new Worker(getUniqueId())

        def asynchronous (priority: Int) (task: => Unit): Unit = queue.synchronized {
            queue += Task(priority, () => task)
            queue.notify
        }

        class Worker (id: Long) extends Thread {

            setDaemon(true)

            def poll (): Task = queue.synchronized {
                while (queue.isEmpty) queue.wait
                queue.head
            }

            override def run(): Unit = while (true) {
                poll().body()
            }
        }

        workers.map(_.start())

    }

}