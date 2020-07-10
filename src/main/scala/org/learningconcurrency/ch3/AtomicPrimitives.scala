package org.learningconcurrency
package ch3

import java.util.concurrent.atomic.AtomicLong
import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicBoolean

/**
  * An atomic variable is a memory location that supports
  * complex linearizable operations (any operation that appears
  * to occur instantaneously to the rest of the system). For example,
  * a volatile write is a linearizable operation, whilst two reads and/or
  * writes is a complex linearizable operation
  */
object AtomicUid extends App {
    private val uid = new AtomicLong(0L)
    // incrementAndGet is a complex linearizable operation
    def getUniqueId (): Long = uid.incrementAndGet()
    execute {
        log(s"Uid asynchronously: ${getUniqueId()}")
    }
    log(s"Got a unique id: ${getUniqueId()}")
}

/**
  * The Compare and Set operation (CAS) is a fundamental building block
  * for lock-free programming. It receives an expected current value and a
  * new value to be set in the atomic variable. If the expected and actual current
  * value are equal, the current value is replaced by the new value.
  */
object AtomicCASUid extends App {

    private val uid = new AtomicLong(0L)

    @tailrec
    def getUniqueId (): Long = {
        val oldUid = uid.get
        val newUid = oldUid + 1
        if (uid.compareAndSet(oldUid, newUid)) newUid
        else getUniqueId()
    }

    execute {
        log(s"Uid asynchronously: ${getUniqueId()}")
    }
    log(s"Got a unique id: ${getUniqueId()}")
}

/**
  * Not all operations composed from atomic primitives are lock-free.
  * Atomic variables is necessary but not sufficient for the lock-freedom condition.
  */
object AtomicLock extends App {
    private val lock = new AtomicBoolean(false)

    def mySynchronized (body: => Unit): Unit = {
        while (!lock.compareAndSet(false, true)) {}
        try body
        finally lock.set(false)
    }

    var cnt = 0
    for (i <- 0 until 10) execute {
        mySynchronized { cnt += 1 }
    }
    Thread.sleep(1000)
    log(s"Count is $cnt")
}