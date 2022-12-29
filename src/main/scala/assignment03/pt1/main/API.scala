package assignment03.pt1.main

import akka.actor.typed.ActorRef
import assignment03.pt1.main.Boundary
import assignment03.pt1.main.Body.Body

object API:
  enum API:
    case UpdateGUI(vt: Double, currentIteration: Int, bodies: Array[Body], bounds: Boundary, from: ActorRef[Msg])
    case Msg(whom: String, bodies: Array[Body], to: ActorRef[Messaged])
    case Messaged(whom: String, bodiesToUpdate: Array[Body], from: ActorRef[Msg])
    case Start
    case Stop
