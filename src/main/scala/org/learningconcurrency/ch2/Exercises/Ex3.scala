package org.learningconcurrency
package ch2
package Exercises

object Ex3 extends App {
    
    class SyncVar [T] {
        private var variable: Option[T] = None

        def get (): Either[String, T] = this.synchronized {
            variable match {
                case Some(value) => {
                    variable = None
                    Right(value)
                }
                case None => Left("Called get for an empty variable")
            }
        }

        def put (t: T): Either[String, Unit] = this.synchronized {
            variable match {
                case Some(value) => Left("Called put for an existing value")
                case None => Right(variable = Some(t))
            }
        }

        def isEmpty (): Boolean = this.synchronized{variable.isEmpty}

        def nonEmpty ():Boolean = this.synchronized{!isEmpty()}
    }

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
