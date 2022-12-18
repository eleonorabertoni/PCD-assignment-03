package it.unibo.pcd.akka.basics.pt1

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}
import akka.actor.typed.scaladsl.AskPattern.Askable

import concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

object ProvaPrinter {
  enum API:
    case Tick
    case Stop
    case Restart
    case Print(whom: String, to: ActorRef[Printed])
    case Printed(whom: String, from: ActorRef[Print])

  /**
   * Printer
   */
  object Printer:
    def apply(): Behavior[API.Print] = Behaviors.receive {
      case (ctx, API.Print("stop", _)) =>
        ctx.log.info("ciaone")
        Behaviors.stopped
      case (ctx, msg) =>
        ctx.log.info("wow")
        Behaviors.same
    }

  /**
   * Stopper
   */
/*
  object Stopper:
    def apply(): Behavior[API] = Behaviors.receive {
      case (ctx, API.Stop) => ! API.Print("STOP DI PROVA", ctx.self)
    }
*/
  /**
   * Counter
   */
  object Counter:
    def apply(from: Int, to: Int): Behavior[API] =
      Behaviors.receive { (context, msg) =>
        msg match {
          case API.Tick if from != to =>
            context.log.info(s"Count: $from")
            Counter(from - from.compareTo(to), to)
          case _ => Behaviors.stopped
        }
      }

}

import ProvaPrinter.*
object TestProvaPrinter extends App:

  val system: ActorSystem[API] = ActorSystem(
    Behaviors.setup[API] { ctx =>
      Behaviors.withTimers { timers =>
        var printer1: ActorRef[API.Print] = ctx.spawn(Printer(), "printer1")
        //val stopper =
        Behaviors.receiveMessage {
          case API.Print("Inizio", _) =>
            printer1 ! API.Print("ciao print 1", ctx.self)
            Behaviors.same
          case API.Stop =>
            printer1 ! API.Print("stop", ctx.self)
            timers.cancel("k")
            Behaviors.same
          case API.Print("start", s) =>
            ctx.log.info(""+s)
            printer1 = ctx.spawn(Printer(), "printer2")
            printer1 ! API.Print("ciao print 1", ctx.self)
            timers.startSingleTimer("k", API.Print("cosa", ctx.self), 300.millis)
            Behaviors.same
          case API.Print(s, _) =>
            ctx.log.info(""+s)
            printer1 ! API.Print("ciao print 1", ctx.self)
            //system ! API.Stop
            timers.startSingleTimer("k", API.Print("cosa", ctx.self), 300.millis)
            Behaviors.same
          case _ => Behaviors.same
        }
      }
    },
    name = "hello-world"
  )
  system ! API.Print("Inizio", system.ignoreRef)
  Thread.sleep(1000)
  system ! API.Print("prova", system.ignoreRef)
  Thread.sleep(3000)
  system ! API.Stop
  system ! API.Print("start", system.ignoreRef)
  Thread.sleep(1000)
  system ! API.Stop

  /**
   * Possiamo fare system ! dentro a receive
   *
   * case Messaggio sulla velocità per ogni body o gruppo di body ?
   * ...
   * case Messaggio sulla forza
   * ...
   *
   * case Messaggio su accelerazione
   * devo avere future su velocità e forza (di tutti i body?) --> perchè non so se le future posso metterle dentor un attore separato
   */



