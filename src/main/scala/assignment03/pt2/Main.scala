package assignment03.pt2

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AskPattern, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import assignment03.pt1.main.P2d
import assignment03.pt2.API.API
import assignment03.pt2.RainSensorActor
import assignment03.pt2.RainSensorActor.{simulationIncrement, simulationOscillation}
import assignment03.pt2.Root.{HubServiceKey, SensorsServiceKey, ViewServiceKey}
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}

import scala.concurrent.duration.DurationInt
import java.util
import java.util.ArrayList
import scala.concurrent.{ExecutionContext, Future}

// PERCHE DEVONO PARTIRE I DUE SEED

/** Zone 0 **/
@main def hubZone0: Unit =
  startupWithRole("hub", seeds.head)(Root(P2d(1,1), 30, 0, HubServiceKey, ViewServiceKey, SensorsServiceKey))
  
@main def backend0Zone0: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("backend", seeds.last)(Root(P2d(0,0),simulationIncrement(2), Option(it), 0, SensorsServiceKey, HubServiceKey))

@main def backend1Zone0: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("backend", 8080)(Root(P2d(0,0),simulationIncrement(5), Option(it), 1, SensorsServiceKey, HubServiceKey))

@main def backend2Zone0: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("backend", 8081)(Root(P2d(0,0), simulationOscillation(rand, 5), Option(it), 2, SensorsServiceKey, HubServiceKey))
