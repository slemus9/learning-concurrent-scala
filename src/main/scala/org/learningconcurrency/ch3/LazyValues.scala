package org.learningconcurrency
package ch3

/**
  * A lazy value should not be initialized with an expression 
  * that depends on the state of the program. The non value
  * does not follow this rule. Lazy values are affected by 
  * non-determinism. 
  */
object LazyValsCreate extends App {
    lazy val obj = new AnyRef
    lazy val non = s"Made by ${Thread.currentThread.getName}"
    execute {
        log(s"EC sees obj = $obj")
        log(s"EC sees non = $non")
    }
    log(s"Main sees obj = $obj")
    log(s"Main sees non = $non")
    Thread.sleep(500)
}

/**
  * In Scala, singleton objects are implemented
  * as lazy values.
  */
object LazyValObject extends App {
    object  Lazy { log("Running Lazy constructor") }
    log("Main thread is about to reference Lazy")
    Lazy
    log("Main thread completed")
}

/**
  * In Scala, lazy values are implemented with the
  * "double-checked locking idiom"; a concurrent programming pattern
  * that ensures that a lazy value is initialized by at most one thread
  * when is first accessed. This ensures that lazy values are both thread-safe
  * and efficient.
  */
object LazyValsUnderTheHood extends App {
    @volatile
    private var _bitmap = false
    private var _obj: AnyRef = _

    def obj = if (_bitmap) _obj else this.synchronized {
        if (!_bitmap) {
            _obj = new AnyRef
            _bitmap = true
        }
        _obj
    }

    log(s"$obj")
    log(s"$obj")
}

/**
  * Synchronization on the enclosing object might cause
  * problems. Avoid cyclic dependencies between lazy values
  * since they can cause deadlocks in concurrent programming
  * (stack overflows in sequential programming)
  */
object LazyValsDeadlock extends App {
    object A { lazy val x: Int = B.y }
    object B { lazy val y: Int = A.x }
    execute { B.y }
    A.x
}

/**
  * Don't invoke blocking operations inside lazy value
  * initialization expressions or singleton object
  * constructors.
  */
object LazyValsAndBlocking extends App {
    lazy val x: Int = {
        val t = ch2.thread { println(s"Initializing $x") }
        t.join
        1
    }
    x
}

/**
  * The main thread acquires the monitor of the object and
  * starts a new thread. When this thread tries to initialize x, it'll 
  * result in a deadlock since the object is already blocked by the main
  * thread.
  * 
  * Never call synchronized on publicly available objects, always use a
  * dedicated, private, dummy object for synchronization. Keeping locks private
  * reduces the chances of getting a deadlock.
  */
object LazyValsAndMonitors extends App {
    lazy val x = 1
    this.synchronized {
        val t = ch2.thread { x }
        t.join()
    }
}