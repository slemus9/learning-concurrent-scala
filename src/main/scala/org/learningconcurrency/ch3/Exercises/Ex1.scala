package org.learningconcurrency
package ch3
package Exercises

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.Failure
import scala.util.Success

object Ex1 extends App {

    class PiggybackContext extends ExecutionContext {

        def execute(runnable: Runnable): Unit = Try(runnable.run()) match {
            case Failure(exception) => reportFailure(exception)
            case Success(value) => log(s"Success: ${value}")
        }

        def reportFailure(cause: Throwable): Unit = 
            log(s"Error ${cause.getMessage()}")
    }

}
