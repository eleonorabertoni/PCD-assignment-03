package it.unibo.pcd.akka.basics.pt1

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}
import akka.actor.typed.scaladsl.AskPattern.Askable
import it.unibo.pcd.akka.basics.pt1.Body.Body
import it.unibo.pcd.akka.basics.pt1.V2d.V2d

import concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Success}


/**
 * Si calcolano forze, accelerazione, velocità
 * POI
 * posizione e collisione sui bordi
 *
 * POI
 * virtual tiem e GUIF
 */
import Utils.computeTotalForceOnBody

object ProvaFuture:

  enum API:
    case Tick
    case Stop
    case Restart
    case Msg(whom: String, to: ActorRef[Messaged])
    case Messaged(whom: String, from: ActorRef[Msg])

  // TODO
  object Messager:
    def apply(start: Int, end: Int, bodies: Seq[Body]): Behavior[API.Msg] = Behaviors.receive {
      case (ctx, API.Msg("stop", _)) =>
        ctx.log.info("ciaone")
        Behaviors.stopped
      case (ctx, API.Msg("Forza", repl)) =>
        val v = for i <- start until end yield computeTotalForceOnBody(bodies(i), bodies)
        repl ! API.Messaged(v.toString(), ctx.self) // devo inviare ciò a chi aggiorna
        Behaviors.same
      case (ctx, msg) =>
        ctx.log.info("wow"+msg)
        msg.to ! API.Messaged("stop", ctx.self)
        Behaviors.same
    }

import ProvaFuture.*
import Utils.createBodies
object TestProvaFuture extends App:
  val N_ACTORS: Int = 4 //?
  val N_BODY: Int = 10

  // TODO ACTOR SYSTEM
  val system: ActorSystem[API] = ActorSystem(
    Behaviors.setup[API] { ctx =>
      Behaviors.withTimers { timers =>
        val bounds: Boundary = Boundary(-50, -50, 50, 50)
        var actors: Seq[ActorRef[API.Msg]] = List.empty
        val bodies: Seq[Body] = createBodies(N_BODY, bounds)
        for i <- 0 until N_ACTORS do actors = actors :+ ctx.spawn(Messager(bodies.size * i / N_ACTORS, bodies.size * (i + 1) / N_ACTORS, bodies), "printer" + i)

        Behaviors.receiveMessage{
          case API.Msg("Inizio", msg) =>
            for a <- actors do a ! API.Msg("babbuota", ctx.self)
            Behaviors.same
          case _ =>
            println("Bella")
            //for a <- actors do a ! Print("babbuota", ctx.self)
            Behaviors.same
          // tutti gli attori devono calcolare la velocità sui loro body

          // tutti gli attori devono calcolare la forza sui loro body

          // tutti gli attori devono calcolare l'accelerazione sui loro body

          //UPDATE VIEW
        }
      }
    },
    name = "hello-world"
  )


  system ! API.Msg("Inizio", system.ignoreRef)
  //Thread.sleep(1000)
// TODO DOVE AGGIORNO LA ROBA :c
// TODO I TIMER CI VANNO ?
/**
 * Possiamo fare system ! dentro a receive
 */




