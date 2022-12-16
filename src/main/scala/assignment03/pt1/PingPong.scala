package assignment03.pt1

import akka.actor.PoisonPill
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

import scala.concurrent.duration.DurationInt

enum PingPong:
  case Pong(replyTo: ActorRef[Ping])
  case Ping(replyTo: ActorRef[Pong])

import PingPong.*

object PingPonger:
  def apply(context: ActorContext[PingPong], bounces: Int = 10): Behavior[PingPong] =
    Behaviors.receive {
      case (ctx, msg) =>
        if (bounces - 1 < 0) {
          ctx.log.info("I got tired of pingpong-ing. Bye bye.")
          Behaviors.stopped
        } else {
          msg match {
            case Pong(replyTo) =>
              ctx.log.info("Pong")
              ctx.scheduleOnce(1.second, replyTo, Ping(context.self))
            case Ping(replyTo) =>
              ctx.log.info("Ping")
              ctx.scheduleOnce(1.second, replyTo, Pong(context.self))
          }
          PingPonger(context, bounces - 1)
        }
  }

object PingPongMainSimple extends App:
  val system = ActorSystem[PingPong](Behaviors.setup(PingPonger(_)), "ping-pong")
  system ! Ping(system)

/** Concepts:
  *   - actor hierarchy
  *   - watching children for termination (through signals)
  */
object PingPongMain extends App:
  val system = ActorSystem(
    Behaviors.setup[PingPong] { ctx =>
      // Csystemhild actor creation
      val pingponger = ctx.spawn(Behaviors.setup[PingPong](ctx => PingPonger(ctx, 5)), "ping-ponger")
      // Watching child
      ctx.watch(pingponger)
      ctx.log.info(s"I am the root user guardian. My path is: ${ctx.self.path}")
      Behaviors
        .receiveMessage[PingPong] { msg =>
          pingponger ! msg
          Behaviors.same
        }
        .receiveSignal { case (ctx, t @ Terminated(_)) =>
          ctx.log.info("PingPonger terminated. Shutting down")
          Behaviors.stopped // Or Behaviors.same to continue
        }
    },
    "ping-pong"
  )
  system.log.info(s"System root path: ${system.path.root}")
  system.log.info(s"Top-level user guardian path: ${system.path}")
  system ! Ping(system)
