package org.learningconcurrency
package ch3

import scala.sys.process._

object ProcessRun extends App {
    val command = "ls"
    //!: start the process and return the exitcode (0 - success, 1 - otherwise)
    val exitCode = command.!
    log(s"Command exited with status $exitCode")

    def lineCount (filename: String): Int = {
        //!!: start the process and return the standard output
        val out = s"wc $filename".!!
        out.trim.split(" ").head.toInt
    }

    println(lineCount("./build.sbt"))
}

object ProcessAsync extends App {
    val lsProcess = "ls -R /".run()
    Thread.sleep(1000)
    log("Timeout - killing ls!")
    lsProcess.destroy()
}
