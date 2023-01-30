package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt2.API.API
import assignment03.pt2.GUI.FiremenView

import java.util

object Viewer:

  def apply(view: FiremenView, stationServiceKey: ServiceKey[API]): Behavior[API | Receptionist.Listing] =
    var fireStations: Seq[ActorRef[API]] = Seq()

    Behaviors.setup[API | Receptionist.Listing] { ctx =>
      /**
       * Anonymous actor in charge of receiving notify messages telling if a station actor has spawned or died.
       * It is necessary due to akka bug that prevents an actor to register and subscribe at the same time.
       */
      ctx.spawnAnonymous[Receptionist.Listing] {
        Behaviors.setup { internal =>
          internal.system.receptionist ! Receptionist.Subscribe(stationServiceKey, internal.self)
          Behaviors.receiveMessage {
            case msg: Receptionist.Listing =>
              ctx.self ! msg
              Behaviors.same
          }
        }
      }

      Behaviors.receiveMessage{
        // if a station spawns or dies it updates its local station list
        case stationServiceKey.Listing(listing) =>
          fireStations = listing.toList
          Behaviors.same
        // this means that the number of sensors n in the zone has changed and the view should be updated 
        case API.MsgSensor(n) =>
          view.display(n)
          Behaviors.same
        // this means that the station state or the zone state has changed and the view should be updated 
        case API.Msg(s) =>
          if s == "FREE" || s == "OCCUPIED" then view.setText(s) else view.setZoneLabel(s)
          Behaviors.same
        // this means that while the zone was in a "SOLVING" state the button has been clicked telling that the problem has been solved
        case API.Stop() =>
          fireStations.head ! API.Stop()
          Behaviors.same
        case _ => Behaviors.same

      }
  }
