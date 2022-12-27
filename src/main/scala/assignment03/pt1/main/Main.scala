package assignment03.pt1.main

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.util.Timeout
import assignment03.pt1.GUI.SimulationView
import assignment03.pt1.main.API.API
import assignment03.pt1.main.Body.Body
import assignment03.pt1.main.Utils.computeTotalForceOnBody
import assignment03.pt1.main.V2d.V2d
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}

import java.util
import java.util.ArrayList
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Success}

object Viewer:

  private def toJava(bodies: Seq[Body]): util.ArrayList[Body] =
    val temp = util.ArrayList[Body]()
    for b <- bodies do temp.add(b)
    temp

  def apply(bodies: Seq[Body], bounds: Boundary, view: SimulationView): Behavior[API] =
    var bodiesJ: util.ArrayList[Body] = toJava(bodies)

    Behaviors.receive{
      case (ctx, API.UpdateGUI(vt, it, bodies_s, bounds_s, from)) =>
        bodiesJ = toJava(bodies_s)
        view.setBodies(bodiesJ)
        view.setBounds(bounds_s)
        view.display(vt, it)
        from ! API.Msg("Inizio", Seq(), null) // TODO NON CI VA NULL
        Behaviors.same
    }

object Messenger:

  def apply(start: Int, end: Int, bounds: Boundary, DT:Double): Behavior[API.Msg] = Behaviors.receive {

    case (ctx, API.Msg("Velocity", bodies, repl)) =>
      var bodiesToUpdate: Seq[Body] = List.empty
      for i <- start until end do
        val b = bodies(i)
        val totalForce = computeTotalForceOnBody(b, bodies)
        val acc = V2d(totalForce).scalarMul(1.0 / b.mass)
        bodiesToUpdate = bodiesToUpdate :+ b.updateVelocity(acc, DT)
      repl ! API.Messaged("Velocity", start, end, bodiesToUpdate, ctx.self)
      Behaviors.same

    case (ctx, API.Msg("Position", bodies, repl)) =>
      var bodiesToUpdate: Seq[Body] = List.empty
      for i <- start until end do
        val b = bodies(i)
        bodiesToUpdate = bodiesToUpdate :+ b.updatePos(DT).checkAndSolveBoundaryCollision(bounds)
      repl ! API.Messaged("Position", start, end, bodiesToUpdate, ctx.self)
      Behaviors.same

    case (ctx, msg) => Behaviors.same //TODO

  }

import assignment03.pt1.main.Utils.createBodies

object Main extends App:
  val N_BODY: Int = 100
  val N_ACTORS: Int =  20//?
  val N_ITERATIONS: Int = 300
  val dim: Int = 2

  val system: ActorSystem[API] = ActorSystem(
    Behaviors.setup[API] { ctx =>

        val DT: Double = 0.001
        var currentIteration: Int = 0
        val bounds: Boundary = Boundary(-dim, -dim, dim, dim)
        var actors: Seq[ActorRef[API.Msg]] = List.empty
        val bodies: Seq[Body] = createBodies(N_BODY, bounds)

        import assignment03.pt1.GUI.SimulationView.VisualiserFrame.{setStartHandler, setStopHandler}

        /** View initialized **/
        val simulationSize = 800
        val view = SimulationView(simulationSize, simulationSize)
        setStopHandler(a => { system ! API.Stop })
        setStartHandler(a => {
          system ! API.Start()
        })

        for i <- 0 until N_ACTORS do actors = actors :+ ctx.spawn(Messenger(bodies.size * i / N_ACTORS, bodies.size * (i + 1) / N_ACTORS, bounds, DT), "printer" + i)
        val viewer = ctx.spawn(Viewer(bodies.toList, bounds,view), "viewer")

        Behaviour.ReceiveBehaviour(bounds, actors, bodies.toList, viewer, N_ITERATIONS, ctx).behaviourReceive()
    },
    name = "hello-world"
  )


  system ! API.Msg("Inizio", Seq(), system.ignoreRef)





