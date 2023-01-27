package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API.{API, Alarm, HUB_STATE, Msg, MsgSensor, STATE, Stop}
import assignment03.pt2.API.HUB_STATE.*
import assignment03.pt2.API.STATE.*

import concurrent.duration.FiniteDuration
import concurrent.duration.DurationInt

trait HubActor:
  def createHubBehavior: Behavior[API | Receptionist.Listing]

object HubActor:

  def apply(pos: P2d, period: FiniteDuration, threshold: Double, sensorsServiceKey: ServiceKey[API], viewServiceKey: ServiceKey[API]): HubActor = HubActorImpl(pos, period, threshold, sensorsServiceKey, viewServiceKey)

  class HubActorImpl(pos: P2d, period: FiniteDuration, threshold: Double, sensorsServiceKey: ServiceKey[API], viewServiceKey: ServiceKey[API]) extends HubActor:

    override def createHubBehavior: Behavior[API | Receptionist.Listing] =
      Behaviors.setup[API | Receptionist.Listing] { ctx =>

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
        var state: HUB_STATE = FREE
        var zoneState: STATE = SAMPLING
        Behaviors.receiveMessage {
          case sensorsServiceKey.Listing(listing) if rainSensors.size != listing.size =>
            rainSensors = listing
            if viewService != null then viewService ! MsgSensor(listing.size)
            println("MSG "+ listing)
            Behaviors.same
          case viewServiceKey.Listing(listing) if listing.nonEmpty =>
            println("SET VIEW")
            viewService = listing.head
            viewService ! MsgSensor(rainSensors.size)
            viewService ! Msg(state.toString)
            viewService ! Msg(zoneState.toString)
            Behaviors.same
          case Alarm(msg) if state == FREE =>
            state = OCCUPIED
            zoneState = ALARM
            println(viewService != null)
            if viewService != null then
              viewService ! Msg(state.toString)
              viewService ! Msg(zoneState.toString)
            Thread.sleep(5000)
            println("RISOLVO")
            zoneState = SOLVING
            if viewService!= null then viewService ! Msg(zoneState.toString)
            for r <- rainSensors do r ! Msg(zoneState.toString)
            Behaviors.same
          case Stop() if zoneState == SOLVING =>
            zoneState = SAMPLING
            println("HO RISOLTO")
            for r <- rainSensors do r ! Msg(zoneState.toString)
            state = FREE
            if viewService != null then
              viewService ! Msg(state.toString)
              viewService ! Msg(zoneState.toString)
            println("FREE")
            Behaviors.same
          case _ => Behaviors.same


        }

      }

