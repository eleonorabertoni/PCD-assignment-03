package assignment03.pt2

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API.API
import assignment03.pt2.API.API.*
import assignment03.pt2.API.STATE
import assignment03.pt2.API.STATE.*
import assignment03.pt2.Root.StatsServiceKey

import concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random
// TODO I BIZANTINI SONO DA CONSIDERARE?
trait RainSensorActor

object RainSensorActor:

  def apply(pos:P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, iterator: Option[Iterator[Double]]) = RainSensorActorImpl(pos, period, threshold, simPred, iterator)

  def simulationIncrement(inc: Double): (Double, Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() + inc else n + inc

  def simulationOscillation(rand: Random, num: Int): (Double,  Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() * rand.nextInt(num).toDouble else n + rand.nextInt(num)

  class RainSensorActorImpl(pos: P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, it: Option[Iterator[Double]]) extends RainSensorActor:
      def createRainSensorBehavior(start: Double): Behavior[API | Receptionist.Listing] =
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
          var others: Set[ActorRef[API]] = Set()
          var registered = false
          var state: STATE = SAMPLING/*
          Behaviors.withTimers { timers =>
            timers.startSingleTimer(Measure(0), period)*/
            // TODO CONSENSUS E SPERO NON SI BLOCCHINO
            Behaviors.receiveMessage {/*
              case Measure(l) if state == SAMPLING =>
                 println("MISURA "+l)
                 timers.startSingleTimer(Measure(simPred(l, it)), period)
                 Behaviors.same*/
              case StatsServiceKey.Listing(listing) if others.size != listing.size =>
                others = listing
                println("MSG "+ listing)
                Behaviors.same
              case _ => Behaviors.same



              /*
              case m: Receptionist.Listing =>
                println("MSG "+m)*/
                Behaviors.same

           

            }

          //}

    }
