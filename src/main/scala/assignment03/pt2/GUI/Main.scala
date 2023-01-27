package assignment03.pt2.GUI

import assignment03.pt1.main.Boundary
import akka.actor.typed.ActorSystem
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt2.API.API
import assignment03.pt2.Root.{HubServiceKey, ViewServiceKey}
import assignment03.pt2.{API, Root, Viewer, startupWithRole}

def launch(string: String, port: Int, viewServiceKey: ServiceKey[API], hubServiceKey: ServiceKey[API]): Unit =
  val N_RAIN_SENSORS: Int = 4

  /** View initialized **/
  val simulationSize = 500
  val view = FiremenView(simulationSize, simulationSize)

  startupWithRole("view", port)(Root(view, viewServiceKey, hubServiceKey))

@main def GUIZone0(): Unit =
  launch("Zone 0", 9999, ViewServiceKey, HubServiceKey)



