package assignment03.pt2.GUI

import assignment03.pt1.main.Boundary
import akka.actor.typed.ActorSystem
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt2.API.API
import assignment03.pt2.Root.{StationServiceKeyZone0, StationServiceKeyZone1, ViewServiceKeyZone0, ViewServiceKeyZone1}
import assignment03.pt2.{API, Root, Viewer, startupWithRole}

def launch(name: String, port: Int, viewServiceKey: ServiceKey[API], stationServiceKey: ServiceKey[API]): Unit =

  /** View initialized **/
  val simulationSize = 500
  val view = FiremenView(simulationSize, simulationSize, name, 3)
  
  /** View actor **/
  startupWithRole("view", port)(Root(view, viewServiceKey, stationServiceKey))

@main def GUIZone0(): Unit =
  launch("Zone 0", 9999, ViewServiceKeyZone0, StationServiceKeyZone0)

@main def GUIZone1(): Unit =
  launch("Zone 1", 9998, ViewServiceKeyZone1, StationServiceKeyZone1)




