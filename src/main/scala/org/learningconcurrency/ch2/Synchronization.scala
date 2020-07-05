package org.learningconcurrency
package ch2

object SynchronizedNesting extends App {
    import scala.collection._

    private val transfers = mutable.ArrayBuffer[String]()

    def logTransfer (name: String, n: Int) = transfers.synchronized {
        transfers += s"Transfer to account '$name' = $n"
    }

    class Account (val name: String, var money: Int)

    def add (account: Account, n: Int) = account.synchronized {
        account.money += n
        if (n > 10) logTransfer(account.name, n)
    }

    val jane = new Account("Jane", 100)
    val john = new Account("John", 200)
    val t1 = thread { add(jane, 5) }
    val t2 = thread { add(john, 50) }
    val t3 = thread { add(jane, 70) }
    t1.join
    t2.join
    t3.join
    log(s"--- transfers ---\n$transfers")
}

object SynchronizedDeadlock extends App {
    import SynchronizedNesting.Account

    def send (a: Account, b: Account, n: Int) = a.synchronized {
        b.synchronized {
            a.money -= n
            b.money += n
        }
    }

    val a = new Account("Jack", 1000)
    val b = new Account("Jill", 2000)
    val t1 = thread {
        for (i <- 9 until 100) send(a, b, 1)
    }
    val t2 = thread {
        for (i <- 0 until 100) send(b, a, 1)
    }
    t1.join
    t2.join
    log(s"a = ${a.money}, b = ${b.money}")
}

object SynchronizedAvoidingDeadLock extends App {
    import org.learningconcurrency.ch2.ThreadsProtectedUid.getUniqueId

    class Account (val name: String, var money: Int) {
        val uid = getUniqueId()
    }

    def send (a1: Account, a2: Account, n: Int) {
        def adjust () = {
            a1.money -= n
            a2.money += n
        }

        if (a1.uid < a2.uid) {
            a1.synchronized { a2.synchronized { adjust() } }
        } else a2.synchronized { a1.synchronized { adjust() } }
    }
}

object SynchronizedBadPool extends App {
    import scala.collection._

    private val tasks = mutable.Queue[() => Unit]()

    val worker = new Thread {
        def poll (): Option[() => Unit] = tasks.synchronized {
            if (tasks.nonEmpty) Some(tasks.dequeue()) else None
        }

        override def run(): Unit = while (true) poll() match {
            case Some(task) => task()
            case None => 
        }
    }

    worker.setName("Worker")
    worker.setDaemon(true)
    worker.start()

    def asynchronous (body: => Unit) = tasks.synchronized {
        tasks.enqueue(() => body)
    }

    asynchronous { log("Hello") }
    asynchronous { log("world!") }
    Thread.sleep(5000)
}

object SynchronizedGuardedBlocks extends App {
    val lock = new AnyRef
    var message: Option[String] = None
    val greeter = thread {
        lock.synchronized {
            while (message == None) lock.wait()
            log(message.get)
        }
    }
    lock.synchronized {
        message = Some("Hello")
        lock.notify()
    }
    greeter.join()
}

object  SynchronizedPool extends App {
    import scala.collection._

    private val tasks = mutable.Queue[() => Unit]()

    object Worker extends Thread {
        setDaemon(true)
        
        def poll (): () => Unit = tasks.synchronized {
            while (tasks.isEmpty) tasks.wait()
            tasks.dequeue()
        }

        override def run(): Unit = while (true) {
            val task = poll()
            task()
        }
    }

    Worker.start()
    def asynchronous (body: => Unit) = tasks.synchronized {
        tasks.enqueue(() => body)
        tasks.notify()
    }

    asynchronous { log("Hello") }
    asynchronous { log("World!") }
    Thread.sleep(500)
}

object SynchronizedPoolWithGracefulShutdown {
    import scala.collection._
    import scala.annotation.tailrec

    private val tasks = mutable.Queue[() => Unit]()

    object Worker extends Thread {

        var terminated = false

        def poll (): Option[() => Unit] = tasks.synchronized {
            while (tasks.isEmpty && !terminated) tasks.wait()
            if (!terminated) Some(tasks.dequeue())
            else None
        }

        @tailrec
        override def run(): Unit = poll() match {
            case Some(task) => {
                task()
                run()
            }
            case None => 
        }

        def shutdown () = tasks.synchronized {
            terminated = true
            tasks.notify()
        }
    }
}