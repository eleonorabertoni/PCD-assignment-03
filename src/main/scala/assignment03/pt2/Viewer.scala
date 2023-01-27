package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt2.API.API
import assignment03.pt2.GUI.FiremenView

import java.util

object Viewer:

  def apply(view: FiremenView, hubServiceKey: ServiceKey[API]): Behavior[API | Receptionist.Listing] =
    var fireStations: Seq[ActorRef[API]] = Seq()

    Behaviors.setup[API | Receptionist.Listing] { ctx =>

      ctx.spawnAnonymous[Receptionist.Listing] {
        Behaviors.setup { internal =>
          internal.system.receptionist ! Receptionist.Subscribe(hubServiceKey, internal.self)
          Behaviors.receiveMessage {
            case msg: Receptionist.Listing =>
              ctx.self ! msg
              Behaviors.same
          }
        }
      }

      Behaviors.receiveMessage{
        case hubServiceKey.Listing(listing) =>
          fireStations = listing.toList
          Behaviors.same
        case API.MsgSensor(n) =>
          view.display(n)
          Behaviors.same
        case API.Msg(s) =>
          if s == "FREE" || s == "OCCUPIED" then view.setText(s) else view.setZoneLabel(s)
          Behaviors.same
        case API.Stop() =>
          fireStations.head ! API.Stop()
          Behaviors.same
        case _ => Behaviors.same

      }
  }
