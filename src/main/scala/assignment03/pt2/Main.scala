package assignment03.pt2

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AskPattern, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import assignment03.pt1.main.P2d
import assignment03.pt2.API.API
import assignment03.pt2.RainSensorActor
import assignment03.pt2.RainSensorActor.{simulationIncrement, simulationIncrementAndReset, simulationOscillation}
import assignment03.pt2.Root.{SensorsServiceKeyZone0, SensorsServiceKeyZone1, StationServiceKeyZone0, StationServiceKeyZone1, ViewServiceKeyZone0, ViewServiceKeyZone1}
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}

import scala.concurrent.duration.DurationInt
import java.util
import java.util.ArrayList
import scala.concurrent.{ExecutionContext, Future}

/** Zone 0 **/
@main def stationZone0: Unit =
  startupWithRole("station", seeds.head)(Root(P2d(1,1), 0, StationServiceKeyZone0, ViewServiceKeyZone0, SensorsServiceKeyZone0))

@main def sensor0Zone0: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("sensor", 8082)(Root(P2d(0,0), simulationIncrementAndReset(5), Option(it), 0, SensorsServiceKeyZone0, StationServiceKeyZone0))

@main def sensor1Zone0: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("sensor", 8080)(Root(P2d(0,0), simulationIncrement(3), Option(it), 1, SensorsServiceKeyZone0, StationServiceKeyZone0))

@main def sensor2Zone0: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("sensor", 8081)(Root(P2d(0,0), simulationOscillation(rand, 5), Option(it), 2, SensorsServiceKeyZone0, StationServiceKeyZone0))

/** Zone 1 **/
@main def stationZone1: Unit =
  startupWithRole("station", seeds.last)(Root(P2d(1,1), 1, StationServiceKeyZone1, ViewServiceKeyZone1, SensorsServiceKeyZone1))

@main def sensor0Zone1: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("sensor", 8083)(Root(P2d(0,0),simulationIncrement(2), Option(it), 3, SensorsServiceKeyZone1, StationServiceKeyZone1))