package assignment03.pt1

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
import assignment03.pt1.GUI.SimulationView

import java.util
import java.util.ArrayList
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

object ProvaBodyGlobali:

  enum API:
    case UpdateGUI(vt: Double, currentIteration:Int, bounds: Boundary, from: ActorRef[Msg])
    case Msg(whom: String, to:ActorRef[Messaged])
    case Messaged(whom: String, from: ActorRef[Msg])
    case Start()
    case Stop
    case Restart

  object Viewer:

    private def toJava(bodies: Seq[Body]): util.ArrayList[Body] =
      val temp = util.ArrayList[Body]()
      for b <- bodies do temp.add(b)
      temp

    def apply(simulationSize: Int, bodies: Seq[Body], bounds: Boundary): Behavior[API.UpdateGUI] =
      val view = SimulationView(simulationSize, simulationSize)

      Behaviors.receive{
        case (ctx, API.UpdateGUI(vt, it, bounds_s, from)) =>
          val bodiesJ: util.ArrayList[Body] = toJava(bodies)
          view.setBodies(bodiesJ)
          view.setBounds(bounds_s)
          view.display(vt, it)
          from ! API.Msg("Inizio", null) // TODO NON CI VA NULL
          Behaviors.same
      }

  // TODO
  object Messager:
    // TODO ripassare ogni volta tutti i bodies aggiornati
    def apply(start: Int, end: Int, bounds: Boundary, DT:Double, bodies: Seq[Body]): Behavior[API.Msg] = Behaviors.receive {
      case (ctx, API.Msg("stop", _)) =>
        Behaviors.stopped
      case (ctx, API.Msg("Velocity", repl)) =>
        for i <- start until end do
          val b = bodies(i)
          val totalForce = computeTotalForceOnBody(b, bodies)
          /* compute instant acceleration */
          val acc = V2d(totalForce).scalarMul(1.0 / b.mass)
          /* update velocity */
          b.updateVelocity(acc, DT)
        //println(bodiesToUpdate)
        repl ! API.Messaged("Velocity", ctx.self)
        Behaviors.same

      case (ctx, API.Msg("Position", repl)) =>
        for i <- start until end do
          val b = bodies(i)
          b.updatePos(DT)
          b.checkAndSolveBoundaryCollision(bounds)
        repl ! API.Messaged("Position", ctx.self)
        Behaviors.same
      case (ctx, msg) =>
        //ctx.log.info("wow"+msg)
        // TODO msg.to ! API.Messaged("stop", ctx.self)
        Behaviors.same

    }



import ProvaBodyGlobali.*
import Utils.createBodies
object TestProvaBodyGlobali extends App:
  val N_BODY: Int = 1000
  val N_ACTORS: Int =  15//?
  val N_ITERATIONS: Int = 100
  val dim: Int = 2

  // TODO ACTOR SYSTEM
  val system: ActorSystem[API] = ActorSystem(
    Behaviors.setup[API] { ctx =>

        val DT: Double = 0.001
        var currentIteration: Int = 0
        val bounds: Boundary = Boundary(-dim, -dim, dim, dim)
        var actors: Seq[ActorRef[API.Msg]] = List.empty
        val bodies: Seq[Body] = createBodies(N_BODY, bounds)

        //println(bodies)
        var arrivati: Int = 0
        var vt: Double = 0

        for i <- 0 until N_ACTORS do actors = actors :+ ctx.spawn(Messager(bodies.size * i / N_ACTORS, bodies.size * (i + 1) / N_ACTORS, bounds, DT, bodies), "printer" + i)
        val viewer = ctx.spawn(Viewer(800,bodies, bounds), "viewer")

        Behaviors.receiveMessage{
          case API.Msg("Inizio", _) =>
            for a <- actors do a ! API.Msg("Velocity", ctx.self)
            Behaviors.same
          case API.Messaged("Velocity", msg) => // TODO HO FINITO VEL
            arrivati = arrivati + 1
            if(arrivati == N_ACTORS)
              arrivati = 0
              for a <- actors do a ! API.Msg("Position", ctx.self)
            Behaviors.same
          case API.Messaged("Position", msg) =>
            arrivati = arrivati + 1
            if(arrivati == N_ACTORS)
              arrivati = 0
              vt = vt + DT
              currentIteration = currentIteration + 1
              if (currentIteration <= N_ITERATIONS)
                //println(currentIteration)
                viewer ! API.UpdateGUI(vt, currentIteration, bounds, ctx.self)
                //for a <- actors do a ! API.Msg("Velocity", ctx.self) // TODO ???
              else
                print(currentIteration)
                Behaviors.stopped
            // TODO UCCIDI I FIGLI PERCHè IL PROGRAMMA NON SE FERMA DA SOLO
            Behaviors.same
          case _ =>
            //println("Bella")
            //for a <- actors do a ! Print("babbuota", ctx.self)
            Behaviors.same

      }
    },
    name = "hello-world"
  )


  system ! API.Msg("Inizio",system.ignoreRef)
  //Thread.sleep(1000)

/**
 * Possiamo fare system ! dentro a receive
 */




