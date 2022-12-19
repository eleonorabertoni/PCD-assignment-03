package assignment03.pt1.pt1

import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}
import akka.actor.typed.scaladsl.AskPattern.Askable
import Body.Body
import V2d.V2d

import concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Success}


/**
 * Si calcolano forze, accelerazione, velocità
 * POI
 * posizione e collisione sui bordi
 *
 * POI
 * virtual time e GUI
 */
import Utils.computeTotalForceOnBody

object ProvaFuture:

  enum API:
    case UpdateGUI
    case Stop
    case Restart
    case Msg(whom: String, to: ActorRef[Messaged])
    case Messaged(whom: String, from: ActorRef[Msg])

  // TODO
  object Messager:

    def apply(start: Int, end: Int, bodies: Seq[Body], bounds: Boundary, DT:Double): Behavior[API.Msg] = Behaviors.receive {
      case (ctx, API.Msg("stop", _)) =>
        ctx.log.info("ciaone")
        Behaviors.stopped
      case (ctx, API.Msg("Velocity", repl)) =>
        for i <- start until end do
          val b = bodies(i)
          val totalForce = computeTotalForceOnBody(b, bodies)
          /* compute instant acceleration */
          val acc = V2d(totalForce).scalarMul(1.0 / b.mass)
          /* update velocity */
          b.updateVelocity(acc, DT)
        repl ! API.Messaged("ho finito vel", ctx.self) // devo inviare ciò a chi aggiorna
        Behaviors.same
      case (ctx, API.Msg("Position", repl)) =>
        for i <- start until end do
          val b = bodies(i)
          b.updatePos(DT)
          b.checkAndSolveBoundaryCollision(bounds)
        repl ! API.Messaged("ho finito pos", ctx.self)
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
  val N_ITERATIONS: Int = 10_000

  // TODO ACTOR SYSTEM
  val system: ActorSystem[API] = ActorSystem(
    Behaviors.setup[API] { ctx =>
        val DT: Double = 0.001
        var currentIteration: Int = 0
        val bounds: Boundary = Boundary(-50, -50, 50, 50)
        var actors: Seq[ActorRef[API.Msg]] = List.empty
        val bodies: Seq[Body] = createBodies(N_BODY, bounds)
        var arrivati: Int = 0
        var vt: Double = 0

        for i <- 0 until N_ACTORS do actors = actors :+ ctx.spawn(Messager(bodies.size * i / N_ACTORS, bodies.size * (i + 1) / N_ACTORS, bodies, bounds, DT), "printer" + i)

        Behaviors.receiveMessage{
          case API.Msg("Inizio", msg) =>
            for a <- actors do a ! API.Msg("Velocity", ctx.self)
            Behaviors.same
          case API.Messaged("ho finito vel", msg) =>
            arrivati = arrivati + 1
            println("CALCOLO VEL "+arrivati)
            if(arrivati == N_ACTORS)
              arrivati = 0
              for a <- actors do a ! API.Msg("Position", ctx.self)
              println("ORA CALCOLATE POSIZIONE")
            Behaviors.same
          case API.Messaged("ho finito pos", msg) =>
            arrivati = arrivati + 1
            println("CALCOLO POS "+arrivati)
            if(arrivati == N_ACTORS)
              arrivati = 0
              vt = vt + DT
              println("AGGIORNO vt"+DT)
              println("AGGIORNO vt"+vt)
              println("NUOVO LOOP")
              currentIteration = currentIteration + 1
              if (currentIteration < N_ITERATIONS)
                for a <- actors do a ! API.Msg("Velocity", ctx.self)
              else Behaviors.stopped
            // TODO deve dire di aggiornare la gui
            // TODO UCCIDI I FIGLI PERCHè IL PROGRAMMA NON SE FERMA DA SOLO
            Behaviors.same
          case _ =>
            println("Bella")
            //for a <- actors do a ! Print("babbuota", ctx.self)
            Behaviors.same

      }
    },
    name = "hello-world"
  )


  system ! API.Msg("Inizio", system.ignoreRef)
  //Thread.sleep(1000)

/**
 * Possiamo fare system ! dentro a receive
 */




