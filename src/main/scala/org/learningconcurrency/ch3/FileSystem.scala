package org.learningconcurrency
package ch3

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import java.util.concurrent.LinkedBlockingQueue
import java.io.File
import scala.collection._
import java.util.concurrent.ConcurrentHashMap
import scala.collection.convert.decorateAsScala._
import org.apache.commons.io.FileUtils

sealed trait State
class Idle extends State
class Creating extends State
/**
  * @param n - Number of concurrent copies being made
  */
class Copying (val n: Int) extends State
class Deleting extends State

class FileSystem (val root: String) {

    val rootDir = new File(root)

    /**
      * Unlike a concurrent queue, a concurrent map does not contain
      * blocking operations, since it's not used in a producer-consumer
      * pattern style. The TrieMap implementation offers consistent iterators.
      */
    val files: concurrent.Map[String, Entry] = concurrent.TrieMap()
    for (f <- FileUtils.iterateFiles(rootDir, null, false).asScala) {
        files.put(f.getName, new Entry(false))
    }


    /**
      * An unbounded concurrent queue
      */
    private val messages = new LinkedBlockingQueue[String]

    /**
      * The take method is blocking. Blocks the logger thread
      * util there is a message in the queue
      */
    val logger = new Thread {
        setDaemon(true)
        override def run(): Unit = while (true) log(messages.take())
    }
    logger.start()
    
    def logMessage (msg: String): Unit = messages.offer(msg)


    class Entry (val isDir: Boolean) {
        val state = new AtomicReference[State](new Idle)
    }


    @tailrec
    private def prepareForDelete (entry: Entry): Boolean = {
        val s0 = entry.state.get
        s0 match {
            case i: Idle =>
                if (entry.state.compareAndSet(i, new Deleting)) true
                else prepareForDelete(entry)
            case c: Creating => {
                logMessage("File currently created. Cannot delete.")
                false
            }
            case c: Copying => {
                logMessage("File currently copied. Cannot delete.")
                false
            }
            case d: Deleting => false
        }
    }

    def deleteFile (filename: String): Unit = {
        files.get(filename) match {
            case None => logMessage(s"Path $filename does not exist.")
            case Some(entry) if entry.isDir => 
                logMessage(s"Path $filename is a directory.")
            case Some(entry) => execute {
                if (prepareForDelete(entry)) {
                    if (FileUtils.deleteQuietly(new File(filename))) {
                        files.remove(filename)
                    }
                }
            }
        }
    }

    @tailrec private def acquire (entry: Entry): Boolean = {
        val s0 = entry.state.get
        s0 match {
            case _: Creating | _: Deleting => {
                logMessage("File inaccessible. Cannot copy.")
                false
            }
            case i: Idle => 
                if (entry.state.compareAndSet(s0, new Copying(1))) true
                else acquire(entry)
            case c: Copying => 
                if (entry.state.compareAndSet(s0, new Copying(c.n + 1))) true
                else acquire(entry)
        }
    }

    @tailrec private def release (entry: Entry): Unit = {
        val s0 = entry.state.get
        s0 match {
            case c: Creating => 
                if (!entry.state.compareAndSet(c, new Idle)) release(entry)
            case c: Copying => {
                val nstate = if (c.n == 1) new Idle else new Copying(c.n - 1)
                if (!entry.state.compareAndSet(c, nstate)) release(entry)
            }
            case _ =>
        }
    }

    def copyFile (src: String, dest: String): Unit = {
        files.get(src) match {
            case Some(srcEntry) if !srcEntry.isDir => execute {
                if (acquire(srcEntry)) try {
                    val destEntry = new Entry(false)
                    destEntry.state.set(new Creating)
                    if (files.putIfAbsent(dest, destEntry) == None) try {
                        FileUtils.copyFile(new File(src), new File(dest))
                    } finally release(destEntry)
                } finally release(srcEntry)
            } 
        }
    }

    def allFiles (): Iterable[String] = for {
        (name, state) <- files
    } yield name

}
