package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API.{API, Alarm, HUB_STATE, Msg}
import assignment03.pt2.API.HUB_STATE.*
import assignment03.pt2.Root.StatsServiceKey

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
            Behaviors.receiveMessage {
              case msg: Receptionist.Listing =>
                ctx.self ! msg
                Behaviors.same
            }
          }
        }

        var rainSensors: Set[ActorRef[API]] = Set()
        var state: HUB_STATE = FREE
        Behaviors.receiveMessage {
          case StatsServiceKey.Listing(listing) if rainSensors.size != listing.size =>
            rainSensors = listing
            println("MSG "+ listing)
            Behaviors.same
          case Alarm(msg) if state == FREE =>
            state = OCCUPIED
            println("HUB " + msg)
            println("RISOLVO")
            for r <- rainSensors do r ! Msg("SOLVING")
            Thread.sleep(10000)
            println("HO RISOLTO")
            for r <- rainSensors do r ! Msg("OK")
            Behaviors.same
          case _ => Behaviors.same


        }

      }

