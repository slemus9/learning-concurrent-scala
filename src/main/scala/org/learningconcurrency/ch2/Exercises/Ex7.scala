package org.learningconcurrency
package ch2
package Exercises

import org.learningconcurrency.ch2.ThreadsProtectedUid.getUniqueId

object Ex7 extends App {

    class Account (val name: String, var money: Int) {
        val uid = getUniqueId()
    }

    def send (a1: Account, a2: Account, n: Int) {
        def adjust () = {
            a1.money -= n
            a2.money += n
        }

        if (a1.uid < a2.uid) {
            a1.synchronized { a2.synchronized { adjust() } }
        } else a2.synchronized { a1.synchronized { adjust() } }
    }

    def sendAll (accounts: Set[Account], target: Account): Unit = target.synchronized {
        for (a <- accounts.toList.sortBy(_.uid)) 
            send(a, target, a.money)
    }
   
    val accounts = (1 to 100).map((i) => new Account(s"Account: $i",i*10)).toSet
    val target = new Account("Target account", 0)
    
    sendAll(accounts,target)

    accounts.foreach((a) => log(s"${a.name}, money = ${a.money}"))
    log(s"${target.name} money = ${target.money}")
}
