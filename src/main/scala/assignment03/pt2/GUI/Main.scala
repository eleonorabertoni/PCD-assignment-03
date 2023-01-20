package assignment03.pt2.GUI

import assignment03.pt1.main.Boundary
import akka.actor.typed.ActorSystem
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt2.API.API
import assignment03.pt2.Root.{FireStationServiceKey, ViewServiceKey}
import assignment03.pt2.Viewer

object Main extends App:
  val N_RAIN_SENSORS: Int = 4
  val N_ITERATIONS: Int = 10_000
  val dim: Int = 3

  val system: ActorSystem[API] = ActorSystem(
    Behaviors.setup[API] { ctx =>

      val DT: Double = 0.001
      var currentIteration: Int = 0
      val bounds: Boundary = Boundary(-dim, -dim, dim, dim)

      /** View initialized **/
      val simulationSize = 800
      val view = FiremenView(simulationSize, simulationSize)
      view.setBounds(bounds)
      //setStopHandler(a => { system ! API.Stop })

      val viewer = ctx.spawn(Viewer(bounds, view), "viewer")
      ctx.system.receptionist ! Receptionist.Register(ViewServiceKey, viewer)
      Behaviors.same

    },
    name = "application"
  )

