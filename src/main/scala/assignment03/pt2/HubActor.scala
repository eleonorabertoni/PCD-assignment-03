package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API.{API, Alarm, HUB_STATE, MsgSensor, Station, Zone, STATE}
import assignment03.pt2.API.HUB_STATE.*
import assignment03.pt2.API.STATE.*
import assignment03.pt2.Root.{StatsServiceKey, ViewServiceKey}

import concurrent.duration.FiniteDuration
import concurrent.duration.DurationInt

trait HubActor:
  def createHubBehavior: Behavior[API | Receptionist.Listing]

object HubActor:

  def apply(pos: P2d, period: FiniteDuration, threshold: Double): HubActor = HubActorImpl(pos, period, threshold)

  class HubActorImpl(pos: P2d, period: FiniteDuration, threshold: Double) extends HubActor:

    override def createHubBehavior: Behavior[API | Receptionist.Listing] =
      Behaviors.setup[API | Receptionist.Listing] { ctx =>

        ctx.spawnAnonymous[Receptionist.Listing] {
          Behaviors.setup { internal =>
            internal.system.receptionist ! Receptionist.Subscribe(StatsServiceKey, internal.self)
            internal.system.receptionist ! Receptionist.Subscribe(ViewServiceKey, internal.self)
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
          case StatsServiceKey.Listing(listing) if rainSensors.size != listing.size =>
            rainSensors = listing
            if viewService != null then viewService ! MsgSensor(listing.size)
            println("MSG "+ listing)
            Behaviors.same
          case ViewServiceKey.Listing(listing) if listing.nonEmpty =>
            println("SET VIEW")
            viewService = listing.head
            viewService ! MsgSensor(listing.size)
            viewService ! Station(state)
            viewService ! Zone(zoneState)
            Behaviors.same
          case Alarm(msg) if state == FREE =>
            zoneState = ALARM
            state = OCCUPIED
            println(viewService != null)
            if viewService != null then
              viewService ! Station(state)
              viewService ! Zone(zoneState)        // TODO forse non serve il controllo
            Thread.sleep(5000) //TODO simula il tempo che c'è tra allarme e in gestion
            println("IN GESTIONE")
            zoneState = SOLVING
            if viewService != null then viewService ! Zone(zoneState)
            for r <- rainSensors do r ! Zone(zoneState)
            Behaviors.same
          case Zone(s) if state == OCCUPIED =>
            if s == SAMPLING then 
              zoneState = SAMPLING
              println("HO RISOLTO")
              for r <- rainSensors do r ! Zone(zoneState)
              state = FREE
              if viewService != null then
                viewService ! Station(state)
                viewService ! Zone(zoneState)
              println("FREE")
            Behaviors.same
          case _ => Behaviors.same


        }

      }

