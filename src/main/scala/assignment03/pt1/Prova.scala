package assignment03.pt1

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

object Prova {
  final case class Print(whom: String, replyTo: ActorRef[Printed])
  final case class Printed(whom: String, from: ActorRef[Print])
  object Printer:
    def apply(): Behavior[Print] = Behaviors.receive { case (ctx, msg) =>
      ctx.log.info(msg.toString)
      Behaviors.same
    }

}

object Counter:
  enum Command: // APIs i.e. message that actors should received / send
    case Tick
    case Tock
  export Command.*
  def apply(from: Int, to: Int): Behavior[Command] =
    Behaviors.receive { (context, msg) =>
      msg match {
        case Tick if from != to =>
          context.log.info(s"Count: $from")
          Counter(from - from.compareTo(to), to)
        case _ => Behaviors.stopped
      }
    }

import Prova.*
object TestProva extends App:
  /*
  def apply(): Behavior[NotUsed] = Behaviors.setup { context =>
    // Create 5 chopsticks
    val chopsticks =
      for (i <- 1 to totalChopsticks)
        yield context.spawn(Chopstick(), "Chopstick" + i)
  * */
  // per usare più attori apply in cui fai spawn

  /**
   * Behaviors.setup[String] { ctx =>
      Behaviors.withTimers { timers =>
        Behaviors.receiveMessage {
          case "" => Behaviors.stopped
          case s =>
            ctx.log.info("" + s.head)
            timers.startSingleTimer(s.tail, 300.millis)
            Behaviors.same
        }
   */
  val system = ActorSystem(
    Behaviors.setup[Print] { ctx =>
      Behaviors.withTimers { timers =>
        val printer = ctx.spawn(Printer(), "printer")
        //val counter = ctx.spawn(Counter(0, 100000), "counter")
        Behaviors.receiveMessage {
          case Print("stop",_) =>
            ctx.log.info("NO")
            //counter ! Counter.Command.Tock
            Behaviors.same
            given Timeout = 2.seconds
            given Scheduler = ctx.system.scheduler
            given ExecutionContext = ctx.executionContext
            val f: Future[Printed] = printer ? (replyTo => Print("start",replyTo))
            f.onComplete {
              case Success(Printed(who, from)) => println(s"$who has been greeted by ${from.path}!")
              case _ => println("No greet")
            }
            Behaviors.empty
          case s =>
            //ctx.log.info("" + s)
            //counter ! Counter.Command.Tick
            timers.startSingleTimer(s, 300.millis)
            Behaviors.same
        }
      }
    },
    name = "hello-world"
  )

  //for i <- 0 to 100 do
  //  Thread.sleep(100)
  system ! Print("boh", system.ignoreRef)
  Thread.sleep(5000)
  system ! Print("stop", system.ignoreRef)
  Thread.sleep(5000)
  system ! Print("start", system.ignoreRef)

