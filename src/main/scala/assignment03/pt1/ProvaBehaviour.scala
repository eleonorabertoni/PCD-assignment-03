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
import Behaviour.behaviourReceive
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

object ProvaBehaviour:
  enum API:
    case UpdateGUI(vt: Double, currentIteration:Int, bodies: Seq[Body], bounds: Boundary, from: ActorRef[Msg])
    //case Velocity(bodies: Seq[Body], to:ActorRef[Messaged])
    //case Position(bodies: Seq[Body], to: ActorRef[Messaged])
    case Msg(whom: String, bodies: Seq[Body], to:ActorRef[Messaged])
    //case Msg(whom: String, to:ActorRef[Messaged])
    case Messaged(whom: String, start: Int, end: Int, bodiesToUpdate: Seq[Body], from: ActorRef[Msg])
    case Start()
    case Stop
    case Restart
    //case Msg(whom: String, to: ActorRef[Messaged])
    //case Messaged(whom: String, from: ActorRef[Msg])

  object Viewer:


    private def toJava(bodies: Seq[Body]): util.ArrayList[Body] =
      val temp = util.ArrayList[Body]()
      for b <- bodies do temp.add(b)
      temp

    def apply(/*simulationSize: Int,*/ bodies: Seq[Body], bounds: Boundary, view: SimulationView): Behavior[API] =
      //val view = SimulationView(simulationSize, simulationSize)
      var bodiesJ: util.ArrayList[Body] = toJava(bodies)


      Behaviors.receive{
        case (ctx, API.UpdateGUI(vt, it, bodies_s, bounds_s, from)) =>
          bodiesJ = toJava(bodies_s)
          view.setBodies(bodiesJ)
          view.setBounds(bounds_s)
          view.display(vt, it)
          from ! API.Msg("Inizio", Seq(), null) // TODO NON CI VA NULL
          Behaviors.same
        //case (ctx, API.Start()) => Behaviors.same
      }

  // TODO
  object Messager:
    // TODO ripassare ogni volta tutti i bodies aggiornati
    def apply(start: Int, end: Int, bounds: Boundary, DT:Double): Behavior[API.Msg] = Behaviors.receive {
      case (ctx, API.Msg("stop", _, _)) =>
        Behaviors.stopped
      case (ctx, API.Msg("Velocity", bodies, repl)) =>
        var bodiesToUpdate: Seq[Body] = List.empty
        for i <- start until end do
          val b = bodies(i)
          val totalForce = computeTotalForceOnBody(b, bodies)
          /* compute instant acceleration */
          val acc = V2d(totalForce).scalarMul(1.0 / b.mass)
          /* update velocity */
          b.updateVelocity(acc, DT)
          bodiesToUpdate = bodiesToUpdate :+ b
        //println(bodiesToUpdate)
        repl ! API.Messaged("Velocity", start, end, bodiesToUpdate, ctx.self)
        Behaviors.same

      case (ctx, API.Msg("Position", bodies, repl)) =>
        var bodiesToUpdate: Seq[Body] = List.empty
        for i <- start until end do
          val b = bodies(i)
          b.updatePos(DT)
          b.checkAndSolveBoundaryCollision(bounds)
          bodiesToUpdate = bodiesToUpdate :+ b
        repl ! API.Messaged("Position", start, end, bodiesToUpdate, ctx.self)
        Behaviors.same
      case (ctx, msg) =>
        //ctx.log.info("wow"+msg)
        // TODO msg.to ! API.Messaged("stop", ctx.self)
        Behaviors.same

    }



import ProvaBehaviour.*
import Utils.createBodies

object TestProvaBehaviour extends App:
  val N_BODY: Int = 100
  val N_ACTORS: Int =  20//?
  val N_ITERATIONS: Int = 100
  val dim: Int = 2

  // TODO ACTOR SYSTEM
  val system: ActorSystem[API] = ActorSystem(
    Behaviors.setup[API] { ctx =>

        val DT: Double = 0.001
        var currentIteration: Int = 0
        val bounds: Boundary = Boundary(-dim, -dim, dim, dim)
        var actors: Seq[ActorRef[API.Msg]] = List.empty
        var bodies: Seq[Body] = createBodies(N_BODY, bounds)

        import assignment03.pt1.GUI.SimulationView.VisualiserFrame.setStopHandler
        import assignment03.pt1.GUI.SimulationView.VisualiserFrame.setStartHandler

        val simulationSize = 800
        val view = SimulationView(simulationSize, simulationSize)
        setStopHandler(a => { system ! API.Stop })
        setStartHandler(a => {
          system ! API.Start()
        })

        //println(bodies)
        var arrivati: Int = 0
        var vt: Double = 0

        for i <- 0 until N_ACTORS do actors = actors :+ ctx.spawn(Messager(bodies.size * i / N_ACTORS, bodies.size * (i + 1) / N_ACTORS, bounds, DT), "printer" + i)
        val viewer = ctx.spawn(Viewer(bodies.toList, bounds,view), "viewer")

        behaviourReceive(bounds, actors, bodies.toList,viewer, N_ACTORS, N_ITERATIONS, ctx)
    },
    name = "hello-world"
  )


  system ! API.Msg("Inizio", Seq(), system.ignoreRef)
  //Thread.sleep(1000)

/**
 * Possiamo fare system ! dentro a receive
 */




