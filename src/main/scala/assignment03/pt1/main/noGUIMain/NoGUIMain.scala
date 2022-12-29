package assignment03.pt1.main.noGUIMain

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler, Terminated}
import akka.util.Timeout
import assignment03.pt1.GUI.SimulationView
import assignment03.pt1.main.API.API
import assignment03.pt1.main.Body.Body
import assignment03.pt1.main.Messenger
import assignment03.pt1.main.Boundary
import assignment03.pt1.main.Utils.{computeTotalForceOnBody, createBodies}
import assignment03.pt1.main.V2d.V2d
import assignment03.pt1.main.noGUIMain.ReceiveBehaviourNoGUI
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}

import java.util
import java.util.ArrayList
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Success}

object NoGUIMain extends App:
  val N_BODY: Int = 1000
  val N_ACTORS: Int =  10
  val N_ITERATIONS: Int = 500
  val dim: Int = 2

  val system: ActorSystem[API] = ActorSystem(
    Behaviors.setup[API] { ctx =>

        val DT: Double = 0.001
        var currentIteration: Int = 0
        val bounds: Boundary = Boundary(-dim, -dim, dim, dim)
        var actors: Array[ActorRef[API.Msg]] = Array()
        val bodies: Array[Body] = createBodies(N_BODY, bounds)

        for i <- 0 until N_ACTORS do actors = actors :+ ctx.spawn(Messenger(bodies.length * i / N_ACTORS, bodies.length * (i + 1) / N_ACTORS, bounds, DT), "printer" + i)
        for a <- actors do ctx.watch(a)
        ReceiveBehaviourNoGUI.ReceiveBehaviourNoGUI(bounds, actors, bodies, N_ITERATIONS, ctx).behaviourReceive()

        .receiveSignal { case (ctx, t @ Terminated(_)) =>
          ctx.log.info("PingPonger terminated. Shutting down")
          Behaviors.stopped // Or Behaviors.same to continue
        }
    },
    name = "hello-world"
  )






