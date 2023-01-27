package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API.{API, Alarm, STATION_STATE, Msg, MsgSensor, STATE, Stop}
import assignment03.pt2.API.STATION_STATE.*
import assignment03.pt2.API.STATE.*

import concurrent.duration.FiniteDuration
import concurrent.duration.DurationInt

trait StationActor:
  def createHubBehavior: Behavior[API | Receptionist.Listing]

object StationActor:

  def apply(pos: P2d, sensorsServiceKey: ServiceKey[API], viewServiceKey: ServiceKey[API]): StationActor = StationActorImpl(pos, sensorsServiceKey, viewServiceKey)

  class StationActorImpl(pos: P2d, sensorsServiceKey: ServiceKey[API], viewServiceKey: ServiceKey[API]) extends StationActor:

    override def createHubBehavior: Behavior[API | Receptionist.Listing] =
      Behaviors.setup[API | Receptionist.Listing] { ctx =>

        /**
         * Anonymous actor in charge of receiving notify messages telling if a sensor/gui actor has spawned or died.
         * It is necessary due to akka bug that prevents an actor to register and subscribe at the same time.
         */
        ctx.spawnAnonymous[Receptionist.Listing] {
          Behaviors.setup { internal =>
            internal.system.receptionist ! Receptionist.Subscribe(sensorsServiceKey, internal.self)
            internal.system.receptionist ! Receptionist.Subscribe(viewServiceKey, internal.self)
            Behaviors.receiveMessage {
              case msg: Receptionist.Listing =>
                ctx.self ! msg
                Behaviors.same
            }
          }
        }

        var rainSensors: Set[ActorRef[API]] = Set()
        var viewService: ActorRef[API] = null 
        var stationState: STATION_STATE = FREE
        var zoneState: STATE = SAMPLING

        Behaviors.receiveMessage {
          // everytime a new sensor spawns or dies it updates the local list and notifies the GUI if present
          case sensorsServiceKey.Listing(listing) if rainSensors.size != listing.size =>
            rainSensors = listing
            if viewService != null then viewService ! MsgSensor(listing.size)
            Behaviors.same
          // when the GUI spawns it saves it locally and it sends the data that need to be shown
          case viewServiceKey.Listing(listing) if listing.nonEmpty =>
            viewService = listing.head
            viewService ! MsgSensor(rainSensors.size)
            viewService ! Msg(stationState.toString)
            viewService ! Msg(zoneState.toString)
            Behaviors.same
          case Alarm(msg) if stationState == FREE =>
            // updates its state and the gui
            stationState = OCCUPIED
            zoneState = ALARM
            if viewService != null then
              viewService ! Msg(stationState.toString)
              viewService ! Msg(zoneState.toString)
            // it simulates the amount of time between the reception of the alarm and the start of the solving process
            Thread.sleep(5000)
            // it updates its state, the gui and all the rain sensors
            zoneState = SOLVING
            if viewService!= null then viewService ! Msg(zoneState.toString)
            for r <- rainSensors do r ! Msg(zoneState.toString)
            Behaviors.same
          case Stop() if zoneState == SOLVING =>
            // this means the flooding has been solved (the GUI button has been clicked)
            // it updates its state, the gui and the sensors
            // for the simulation the sensors will restart from a low value
            zoneState = SAMPLING
            stationState = FREE
            for r <- rainSensors do r ! Msg(zoneState.toString)
            if viewService != null then
              viewService ! Msg(stationState.toString)
              viewService ! Msg(zoneState.toString)
            Behaviors.same
          case _ => Behaviors.same
        }
      }

