package org.learningconcurrency.ch2.Exercises

import scala.collection.mutable
import org.learningconcurrency.ch2.ThreadsProtectedUid.getUniqueId
import scala.annotation.tailrec

object Ex10 {

    case class Task (val priority: Int, body: () => Unit) extends Ordered[Task] {

        def compare (that: Task) = that.priority - this.priority
    }

    class PriorityTaskPool (val p: Int, val important: Int) {

        private val queue = mutable.PriorityQueue[Task]()

        private val workers = for { i <- 1 to p } yield new Worker(getUniqueId())

        def asynchronous (priority: Int) (task: => Unit): Unit = queue.synchronized {
            queue += Task(priority, () => task)
            queue.notify
        }

        class Worker (id: Long) extends Thread {

            private var terminated = false

            def poll (): Option[Task] = queue.synchronized {
                while (queue.isEmpty && !terminated) queue.wait
                if (terminated) None else Some(queue.head)
            }

            @tailrec
            override final def run(): Unit = poll() match {
                case Some(Task(_, body)) if !terminated || p > important => {
                    body()
                    run()
                }
                case None => 
            }

            def shutdown (): Unit = {
                terminated = true
                queue.notify
            }
        }

        def shutdown (): Unit = {
            workers.foreach(_.shutdown())
        }

        workers.map(_.start())

    }

}