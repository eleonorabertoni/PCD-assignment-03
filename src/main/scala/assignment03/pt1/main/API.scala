package assignment03.pt1.main

import akka.actor.typed.ActorRef
import assignment03.pt1.main.Boundary
import assignment03.pt1.main.Body.Body

object API:
  enum API:
    case UpdateGUI(vt: Double, currentIteration: Int, bodies: Seq[Body], bounds: Boundary, from: ActorRef[Msg])
    case Msg(whom: String, bodies: Seq[Body], to: ActorRef[Messaged])
    case Messaged(whom: String, start: Int, end: Int, bodiesToUpdate: Seq[Body], from: ActorRef[Msg])
    case Start()
    case Stop
