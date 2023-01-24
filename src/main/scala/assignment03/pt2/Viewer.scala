package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.Boundary
import assignment03.pt2.API.API
import assignment03.pt2.GUI.FiremenView
import assignment03.pt2.Root.FireStationServiceKey

import java.util

object Viewer:

  def apply(bounds: Boundary, view: FiremenView): Behavior[API | Receptionist.Listing] =
    var fireStations: Seq[ActorRef[API]] = Seq()

    Behaviors.setup[API | Receptionist.Listing] { ctx =>
      ctx.self ! API.UpdateGUI(bounds, ctx.self)

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
        case API.UpdateGUI(bounds_s, from) =>
          view.setBounds(bounds_s)
          //view.display(4)
          ctx.self ! API.UpdateGUI(bounds, ctx.self)
          Behaviors.same
        case API.MsgSensor(n) =>
          println("OH OH OH OHO OJOHOHOHOHO"+n)
          view.display(n)
          Behaviors.same
        case API.Msg(s) =>
          view.setText(s)
          Behaviors.same
        case API.Stop() => // TODO
          println("STOOOOOOOP")
          Behaviors.same
        case _ => Behaviors.same

      }
  }
