package org.learningconcurrency

import scala.concurrent.ExecutionContext

package object ch3 {

    def execute (body: => Unit) = ExecutionContext.global.execute(new Runnable {
        def run (): Unit = body
    })
}
