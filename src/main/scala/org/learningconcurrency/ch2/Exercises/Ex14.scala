package org.learningconcurrency
package ch2
package Exercises

import org.learningconcurrency.ch2.Exercises.Ex11_12_13.ConcurrentBiMap

object Ex14 extends App {
    

    def cache [K, V] (f: K => V): K => V = {
        val resMap = new ConcurrentBiMap[K, V]()
        (k: K) => resMap.getValue(k) match {
            case Some(value) => value
            case None => {
                val f_k = f(k)
                resMap.put(k, f_k)
                f_k
            }
        }
    }

    lazy val fib: Int => BigInt = cache {
        case 0 => 0
        case 1 => 1
        case n => fib(n - 1) + fib(n - 2)
    }

    println(fib(10))

    val workers = for { i <- 1 to 10 } yield thread {
        for (j <- 0 to 1000) {
            val n = (Math.random()*1001).toInt
            val fib_n = fib(n)
            log(s"fib($n) = $fib_n")
        }
    }

    workers.foreach(_.join())

}
