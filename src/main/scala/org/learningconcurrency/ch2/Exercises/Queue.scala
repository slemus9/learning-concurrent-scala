package org.learningconcurrency.ch2.Exercises

object ImmutableQueue {

    case class Queue [A] (
        private val in: List[A], 
        private val out: List[A], 
        size: Int
    ) {
    
        def push (x: A): Queue[A] = this match {
            case Queue(in, out, size) => Queue(x +: in, out, size + 1)
        }
    
        def pop (): (Option[A], Queue[A]) = this match {
            case Queue(in, Nil, size) => in.reverse match {
                case x :: xs => (Some(x), Queue(Nil, xs, size - 1))
                case Nil => (None, this)
            }
            case Queue(in, o :: os, size) => 
                (Some(o), Queue(in, os, size - 1))
        }
    
        def isEmpty (): Boolean = this match {
            case Queue(Nil, Nil, _) => true
            case _ => false
        }
    }
    
    def emptyQueue [A] (): Queue[A] = Queue[A](Nil, Nil, 0)
}
