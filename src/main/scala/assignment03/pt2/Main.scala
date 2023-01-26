package assignment03.pt2

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AskPattern, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import assignment03.pt1.main.P2d
import assignment03.pt2.API.API
import assignment03.pt2.RainSensorActor
import assignment03.pt2.RainSensorActor.{simulationIncrement, simulationOscillation}
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}
import scala.concurrent.duration.DurationInt

import java.util
import java.util.ArrayList
import scala.concurrent.{ExecutionContext, Future}

// PERCHE DEVONO PARTIRE I DUE SEED

@main def hubSeed: Unit =
  startupWithRole("hub", seeds.head)(Root(P2d(1,1), 30, 0))

@main def backendSeed: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("backend", seeds.last)(Root(P2d(0,0),simulationIncrement(5), Option(it), 0))
/*
@main def hub8082: Unit =
  startupWithRole("hub", 8082)(Root(P2d(1,1), 30, 1))
*/
@main def backend8081: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("backend", 8081)(Root(P2d(0,0), simulationIncrement(2), Option(it), 1))

@main def backend8082: Unit =
  val it = Iterator.iterate(1.0)(_ * -1)
  startupWithRole("backend", 8082)(Root(P2d(0,0), simulationIncrement(4), Option(it), 2))


/*


val system: ActorSystem[API] = ActorSystem(
  Behaviors.setup[API] { ctx =>

    val it = Iterator.iterate(1.0)(_ * -1)
    var rainSensors: Array[ActorRef[API]] = Array()
    for i <- 0 until N_RAIN_OSCILLATOR do rainSensors = rainSensors :+ ctx.spawn(RainSensorActor(P2d(i,i), PERIOD, THRESHOLD, simulationOscillation(rand, 10), Option(it)).createRainSensorBehavior, "rain" + i)
    for i <- N_RAIN_OSCILLATOR to N_RAIN_SENSOR - N_RAIN_OSCILLATOR do rainSensors = rainSensors :+ ctx.spawn(RainSensorActor(P2d(i,i), PERIOD, THRESHOLD, simulationIncrement(1), None).createRainSensorBehavior, "rain" + i)
    for r <- rainSensors do r ! API.Start(0, ctx.self)

    Behaviors.receiveMessage{
      case Notify(l, from) if l > THRESHOLD =>
        /*Mi devo allarmare*/
        println(from.toString+" "+l+" "+"O__O")
        Behaviors.same
      case _ => Behaviors.same

    }

      // caserma
      // coordinatore con dentro i pluviometri


      //for i <- 0 until N_ACTORS do actors = actors :+ ctx.spawn(Messenger(bodies.length * i / N_ACTORS, bodies.length * (i + 1) / N_ACTORS, bounds, DT), "printer" + i)

  },
  name = "zone"
)
*/





