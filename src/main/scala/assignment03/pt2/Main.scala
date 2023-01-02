package assignment03.pt2

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AskPattern, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import assignment03.pt1.main.P2d
import assignment03.pt2.API.API
import assignment03.pt2.API.API.*
import assignment03.pt2.RainSensorActor
import assignment03.pt2.RainSensorActor.{simulationIncrement, simulationOscillation}
import it.unibo.pcd.akka.basics.e01hello.Counter
import it.unibo.pcd.akka.basics.e01hello.Counter.Command
import it.unibo.pcd.akka.basics.e01hello.Counter.Command.Tick
import it.unibo.pcd.akka.basics.e06interaction.HelloBehavior.{Greet, Greeted}

import java.util
import java.util.ArrayList
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Success}
import concurrent.duration.DurationInt

object Main extends App:
  val N_RAIN_SENSOR: Int = 4
  val N_RAIN_OSCILLATOR: Int = 1
  val WIDTH: Int = 3
  val HEIGHT: Int = 3
  val rand: Random = Random(999)
  val PERIOD = 500.millis
  val THRESHOLD = 35

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
          println(from.toString+" "+l+" "+"ALLARME")
          Behaviors.same
        case _ => Behaviors.same

      }

        // caserma
        // coordinatore con dentro i pluviometri


        //for i <- 0 until N_ACTORS do actors = actors :+ ctx.spawn(Messenger(bodies.length * i / N_ACTORS, bodies.length * (i + 1) / N_ACTORS, bounds, DT), "printer" + i)

    },
    name = "zone"
  )






