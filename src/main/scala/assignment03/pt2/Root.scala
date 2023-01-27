package assignment03.pt2

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt2.API.API
import akka.cluster.typed.Cluster
import assignment03.pt1.main.{Boundary, P2d}
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.GUI.FiremenView

import concurrent.duration.DurationInt
import assignment03.pt2.RainSensorActor.{simulationIncrement, simulationOscillation}
import assignment03.pt2.StationActor.*

import scala.util.Random

object Root:

  val SensorsServiceKeyZone0: ServiceKey[API] = ServiceKey[API]("StatsService")
  val HubServiceKeyZone0: ServiceKey[API] = ServiceKey[API]("HubService")
  val ViewServiceKeyZone0: ServiceKey[API] = ServiceKey[API]("ViewService")

  val SensorsServiceKeyZone1: ServiceKey[API] = ServiceKey[API]("StatsService1")
  val HubServiceKeyZone1: ServiceKey[API] = ServiceKey[API]("HubService1")
  val ViewServiceKeyZone1: ServiceKey[API] = ServiceKey[API]("ViewService1")

  /**
   * Factory for RainSensor
   */
  def apply(pos: P2d, simPred: (Double, Option[Iterator[Double]])=> Double, it: Option[Iterator[Double]], i: Int, sensorsServiceKey: ServiceKey[API], hubServiceKey: ServiceKey[API]): Behavior[API | Receptionist.Listing] =
    Behaviors.setup { ctx =>
    val rainSensor = ctx.spawn(RainSensorActor(pos, PERIOD, THRESHOLD, simPred, it, sensorsServiceKey, hubServiceKey).createRainSensorBehavior(0), "backend" + i)
    ctx.system.receptionist ! Receptionist.Register(sensorsServiceKey, rainSensor)
    Behaviors.empty
  }

  /**
   * Factory for stations
   *
   */
  def apply(pos: P2d, i: Int, hubServiceKey: ServiceKey[API], viewServiceKey: ServiceKey[API], sensorsServiceKey: ServiceKey[API]): Behavior[API] =
    Behaviors.setup { ctx =>
      val hub = ctx.spawn(StationActor(pos, sensorsServiceKey, viewServiceKey).createHubBehavior, "hub" + i)
      ctx.system.receptionist ! Receptionist.Register(hubServiceKey, hub)
      Behaviors.empty
    }

  /**
   * Factory for viewer
   *
   */
  def apply(view: FiremenView, viewServiceKey: ServiceKey[API], hubServiceKey: ServiceKey[API]): Behavior[API] =
    Behaviors.setup{ ctx =>
      val viewer = ctx.spawn(Viewer(view, hubServiceKey), "viewer")
      view.setDisableButton(a => {viewer ! API.Stop()})
      ctx.system.receptionist ! Receptionist.Register(viewServiceKey, viewer)
      Behaviors.same
    }
