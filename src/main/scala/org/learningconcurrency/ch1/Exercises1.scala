package org.learningconcurrency.ch1

object Exercises1 extends App{
    
    // Ex.1
    def compose [A, B, C] (
        g: B => C,
        f: A => B
    ): A => C = (a: A) => g(f(a))

    // Ex.2
    def fuse [A, B] (a: Option[A], b: Option[B]): Option[(A, B)] =
        for {
            x <- a
            y <- b
        } yield (x, y)

    // Ex.3
    def check [T] (xs: Seq[T]) (pred: T => Boolean): Boolean =
        xs.dropWhile(pred).isEmpty
    
    // Ex.4
    case class Pair [A, B] (val fst: A, val snd: B)

    // Ex.5
    def permutations (xs: String): Seq[String] = 
        if (xs.isEmpty) Seq("")
        else for {
            x <- xs
            val rem = xs.takeWhile(_ != x) + xs.dropWhile(_ != x).tail
            perms <- permutations(rem)
        } yield x +: perms

    println(permutations("asdaefs"))
}
