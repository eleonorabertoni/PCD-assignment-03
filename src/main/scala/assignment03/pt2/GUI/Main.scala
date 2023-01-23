package assignment03.pt2.GUI

import assignment03.pt1.main.Boundary
import akka.actor.typed.ActorSystem
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt2.API.API
import assignment03.pt2.{API, Root, Viewer, startupWithRole}
import assignment03.pt2.Root.{FireStationServiceKey, ViewServiceKey}

object Main extends App:
  val N_RAIN_SENSORS: Int = 4
  val N_ITERATIONS: Int = 10_000
  val dim: Int = 3

  val system: ActorSystem[API] =
      val bounds: Boundary = Boundary(-dim, -dim, dim, dim)

      /** View initialized **/
      val simulationSize = 800
      val view = FiremenView(simulationSize, simulationSize)
      view.setBounds(bounds)
      view.setDisableButton(a => {
        println("NOOOOO")
        system ! API.Stop()})

      startupWithRole("view", 9999)(Root(bounds, view))


