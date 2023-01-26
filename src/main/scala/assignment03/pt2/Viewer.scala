package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt2.API.API
import assignment03.pt2.GUI.FiremenView
import assignment03.pt2.Root.FireStationServiceKey

import java.util

object Viewer:

  def apply(view: FiremenView): Behavior[API | Receptionist.Listing] =
    var fireStations: Seq[ActorRef[API]] = Seq()

    Behaviors.setup[API | Receptionist.Listing] { ctx =>

      ctx.spawnAnonymous[Receptionist.Listing] {
        Behaviors.setup { internal =>
          internal.system.receptionist ! Receptionist.Subscribe(FireStationServiceKey, internal.self)
          Behaviors.receiveMessage {
            case msg: Receptionist.Listing =>
              ctx.self ! msg
              Behaviors.same
          }
        }
      }

      Behaviors.receiveMessage{
        case FireStationServiceKey.Listing(listing) =>
          fireStations = listing.toList
          Behaviors.same
        case API.MsgSensor(n) =>
          println("OH OH OH OHO OJOHOHOHOHO"+n)
          view.display(n)
          Behaviors.same
        case API.Msg(s) =>
          println("MSG")
          println(s)
          if s == "FREE" || s == "OCCUPIED" then view.setText(s) else view.setZoneLabel(s)
          Behaviors.same
        case API.Stop() => // TODO
          println("STOOOOOOOP")
          fireStations.head ! API.Stop()
          Behaviors.same
        case _ => Behaviors.same

      }
  }
