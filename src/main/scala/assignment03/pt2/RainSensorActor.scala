package assignment03.pt2

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API.API
import assignment03.pt2.API.API.*

import concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

object RainSensorActor:

  def apply(pos:P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, iterator: Option[Iterator[Double]]) = RainSensorActorImpl(pos, period, threshold, simPred, iterator)

  def simulationIncrement(inc: Double): (Double, Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() + inc else n + inc

  def simulationOscillation(rand: Random, num: Int): (Double,  Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() * rand.nextInt(num).toDouble else n + rand.nextInt(num)

  class RainSensorActorImpl(pos: P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, it: Option[Iterator[Double]]):

    def createRainSensorBehavior: Behavior[API] =
      Behaviors.setup[API] { ctx =>
        var manager: ActorRef[API] = null
        Behaviors.withTimers { timers =>
          Behaviors.receiveMessage {
            case Start(s, from) =>
              manager = from
              timers.startSingleTimer(Measure(simPred(s,it)), period)
              Behaviors.same
            case Measure(l) =>
              val m = simPred(l,it)
              timers.startSingleTimer(Measure(m), period)
              manager ! Notify(m, ctx.self)
              Behaviors.same
            case _ => Behaviors.stopped

          }
        }
    }
