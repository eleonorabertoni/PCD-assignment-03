package assignment03.pt2

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API.API

import scala.concurrent.duration.FiniteDuration

trait HubActor:
  def createHubBehavior: Behavior[API]

object HubActor:

  def apply(pos: P2d, period: FiniteDuration, threshold: Double): HubActor = HubActorImpl(pos, period, threshold)

  class HubActorImpl(pos: P2d, period: FiniteDuration, threshold: Double) extends HubActor:

    override def createHubBehavior: Behavior[API] =
      Behaviors.setup[API] { ctx =>
        Behaviors.receiveMessage {
          msg =>
            println("HUB " + msg)
            Behaviors.same

        }

      }

