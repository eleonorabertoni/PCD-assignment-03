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
  val StationServiceKeyZone0: ServiceKey[API] = ServiceKey[API]("StationService")
  val ViewServiceKeyZone0: ServiceKey[API] = ServiceKey[API]("ViewService")

  val SensorsServiceKeyZone1: ServiceKey[API] = ServiceKey[API]("StatsService1")
  val StationServiceKeyZone1: ServiceKey[API] = ServiceKey[API]("StationService1")
  val ViewServiceKeyZone1: ServiceKey[API] = ServiceKey[API]("ViewService1")

  /**
   * Factory for RainSensor
   */
  def apply(pos: P2d, simPred: (Double, Option[Iterator[Double]])=> Double, it: Option[Iterator[Double]], i: Int, sensorsServiceKey: ServiceKey[API], stationServiceKey: ServiceKey[API]): Behavior[API | Receptionist.Listing] =
    // It spawns the rain sensor actor and it registers it to the service to notify the subscribed actors when it spawns or dies
    Behaviors.setup { ctx =>
    val rainSensor = ctx.spawn(RainSensorActor(pos, PERIOD, THRESHOLD, simPred, it, sensorsServiceKey, stationServiceKey).createRainSensorBehavior(0), "sensor" + i)
    ctx.system.receptionist ! Receptionist.Register(sensorsServiceKey, rainSensor)
    Behaviors.empty
  }

  /**
   * Factory for stations
   *
   */
  def apply(pos: P2d, i: Int, stationServiceKey: ServiceKey[API], viewServiceKey: ServiceKey[API], sensorsServiceKey: ServiceKey[API]): Behavior[API] =
    // It spawns the station actor and it registers it to the service to notify the subscribed actors when it spawns or dies
    Behaviors.setup { ctx =>
      val station = ctx.spawn(StationActor(pos, sensorsServiceKey, viewServiceKey).createStationBehavior, "station" + i)
      ctx.system.receptionist ! Receptionist.Register(stationServiceKey, station)
      Behaviors.empty
    }

  /**
   * Factory for viewer
   *
   */
  def apply(view: FiremenView, viewServiceKey: ServiceKey[API], stationServiceKey: ServiceKey[API]): Behavior[API] =
    // It spawns the view actor and it registers it to the service to notify the subscribed actors when it spawns or dies
    Behaviors.setup{ ctx =>
      val viewer = ctx.spawn(Viewer(view, stationServiceKey), "viewer")
      // it sets the button handler
      view.setDisableButton(a => {viewer ! API.Stop()})
      ctx.system.receptionist ! Receptionist.Register(viewServiceKey, viewer)
      Behaviors.same
    }
