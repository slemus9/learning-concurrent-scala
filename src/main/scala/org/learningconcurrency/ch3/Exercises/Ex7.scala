package org.learningconcurrency.ch3.Exercises

import scala.collection._

class SyncConcurrentMap [K, V] extends concurrent.Map[K, V] {


    val table = mutable.Map[K, V]()
        
    override def get(key: K): Option[V] = table.synchronized(
        table.get(key)
    )

    override def iterator: Iterator[(K, V)] = table.synchronized(
        table.iterator
    )

    override def +=(kv: (K, V)): SyncConcurrentMap.this.type = table.synchronized { kv match {
        case (k, v) => {
            table.put(k, v)
            this
        }
    }
    }

    override def -=(key: K): SyncConcurrentMap.this.type = table.synchronized { 
        table.remove(key)
        this
    }

    override def putIfAbsent(k: K, v: V): Option[V] = table.synchronized { 
        table.get(k) match {
            case None => {
                table.put(k, v)
                Some(v)
            }
            case value @ Some(_) => value
        }
    }

    override def remove(k: K, v: V): Boolean = table.synchronized {
        table.get(k) match {
            case None => false
            case Some(value) => 
                if (v == value) {
                    table.remove(k)
                    true
                } else false 
        }
    }

    override def replace(k: K, oldvalue: V, newvalue: V): Boolean = table.synchronized {
        table.get(k) match {
            case None => false
            case Some(actualValue) => 
                if (oldvalue != actualValue) false
                else {
                    table.put(k, newvalue)
                    true
                } 
        }
    }

    override def replace(k: K, v: V): Option[V] = table.synchronized {
        table.get(k) match {
            case None => None
            case oldVal @ Some(_) => {
                put(k, v)
                oldVal
            }
        }
    }
}

object Ex7 {
  

}
