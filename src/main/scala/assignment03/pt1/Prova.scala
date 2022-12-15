package assignment03.pt1

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import concurrent.duration.DurationInt

object Prova {
  final case class Print(whom: String, replyTo: ActorRef[Printed])
  final case class Printed(whom: String, from: ActorRef[Print])
  object Printer:
    def apply(): Behavior[Print] = Behaviors.receive { case (ctx, msg) =>
      ctx.log.info(msg.toString)
      Behaviors.same
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
        ctx.spawn(Printer(), "boh")
        Behaviors.receiveMessage {
          case s =>
            ctx.log.info("" + s)
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