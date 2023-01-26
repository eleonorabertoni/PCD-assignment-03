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
import assignment03.pt2.HubActor.*

import scala.util.Random

object Root:
  val SensorsServiceKey: ServiceKey[API] = ServiceKey[API]("StatsService") //TODO per zona
  val HubServiceKey: ServiceKey[API] = ServiceKey[API]("HubService")
  val FireStationServiceKey: ServiceKey[API] = ServiceKey[API]("FireStation") // TODO per tutti gli hub
  val ViewServiceKey: ServiceKey[API] = ServiceKey[API]("ViewService") // TODO per tutti quelli che devono parlare con la view

  /**
   * Factory for RainSensor
   */
  def apply(pos: P2d, simPred: (Double, Option[Iterator[Double]])=> Double, it: Option[Iterator[Double]], i: Int): Behavior[API | Receptionist.Listing] =
    Behaviors.setup { ctx =>
    val rainSensor = ctx.spawn(RainSensorActor(pos, PERIOD, THRESHOLD, simPred, it).createRainSensorBehavior(0), "backend" + i)
    ctx.system.receptionist ! Receptionist.Register(SensorsServiceKey, rainSensor)
    Behaviors.empty
  }

  /**
   * Factory for stations
   *
   */
  def apply(pos: P2d, threshold: Double, i: Int): Behavior[API] =
    Behaviors.setup { ctx =>
      //val cluster = Cluster(ctx.system)
      val hub = ctx.spawn(HubActor(pos, 1000.millis, threshold).createHubBehavior, "hub" + i)
      //if (cluster.selfMember.hasRole("hub"))
      ctx.system.receptionist ! Receptionist.Register(HubServiceKey, hub)
      ctx.system.receptionist ! Receptionist.Register(FireStationServiceKey, hub)
      Behaviors.empty
    }

  def apply(view: FiremenView): Behavior[API] =
    Behaviors.setup{ ctx =>
      val viewer = ctx.spawn(Viewer(view), "viewer")
      view.setDisableButton(a => {
        println("NOOOOO")
        viewer ! API.Stop()})
      ctx.system.receptionist ! Receptionist.Register(ViewServiceKey, viewer)
      Behaviors.same
    }
