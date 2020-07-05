package org.learningconcurrency
package ch2
package Exercises

import scala.collection.mutable

object Ex11_12_13 extends App {

    class ConcurrentBiMap [K, V] {

        private val table = mutable.Map[K, V]()

        def put (k: K, v: V): Option[V] = table.synchronized {
            table.put(k, v)
        }

        def removeKey (k: K): Option[V] = table.synchronized {
            table.remove(k)
        }

        def getValue (k: K): Option[V] = table.synchronized {
            table.get(k)
        }

        def getKey (v: V): Option[K] = table.synchronized {
            iterator.find(p => p._2 == v) match {
                case Some((k, _)) => Some(k)
                case None => None 
            }
        }

        def size: Int = table.synchronized {
            table.size
        }

        def iterator: Iterator[(K, V)] = table.synchronized {
            table.toIterator
        }

        def replace (k1: K, v1: V, k2: K, v2: V): Unit = table.synchronized {
            table.put(k1, v2)
            table.put(k2, v1)
        }

        def keys (): IndexedSeq[K] = table.synchronized {
            table.keys.toIndexedSeq
        }

    }

    val concurrentMap = new ConcurrentBiMap[Long, Int]()
    var currKey = 0
    val threads1 = for { i <- 1 to 5 } yield thread {
        for (j <- 1 to 1000) {
            val v = (Math.random()*1000).toInt
            concurrentMap.put(currKey, v)
            log(s"Inserted: (k, v) = ($currKey, $v)")
            currKey += 1
        }
    }

    threads1.foreach(_.join())

    val keys = concurrentMap.keys()
    val threads2 = for { i <- 1 to 5 } yield thread {
        concurrentMap.iterator.foreach {case (k1, v1) => 
            val idx = (Math.random()*keys.size).toInt 
            val k2 = keys(idx)
            val v2 = concurrentMap.getValue(k2) match {
                case Some(v) => v
                case None => (Math.random()*1000).toInt
            }
            concurrentMap.replace(k1, v1, k2, v2)
            log(s"Replacing: ($k1, $v1) <-> ($k2, $v2)")
        }
    }

}
