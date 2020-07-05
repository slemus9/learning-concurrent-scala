package org.learningconcurrency.ch2.Exercises

import scala.collection.mutable

object Ex8 {
  
    case class Task (val priority: Int, body: () => Unit) extends Ordered[Task] {

        def compare (that: Task) = that.priority - this.priority
    }


    class PriorityTaskPool {

        private val queue = mutable.PriorityQueue[Task]()

        def asynchronous (priority: Int) (task: => Unit): Unit = queue.synchronized {
            queue += Task(priority, () => task)
            queue.notify
        }

        object Worker extends Thread {
            
            setDaemon(true)

            def poll (): Task = queue.synchronized {
                while (queue.isEmpty) queue.wait
                queue.head
            }

            override def run(): Unit = while (true) {
                val tsk = poll()
                tsk.body()
            }
        }

    }

}
