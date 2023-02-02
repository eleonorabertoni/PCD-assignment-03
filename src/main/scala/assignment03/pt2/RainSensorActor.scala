package assignment03.pt2

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API
import assignment03.pt2.API.*
import assignment03.pt2.API.STATE
import assignment03.pt2.API.STATE.*

import concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

trait RainSensorActor

object RainSensorActor:

  def apply(pos:P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, iterator: Option[Iterator[Double]], sensorsServiceKey: ServiceKey[API], stationServiceKey: ServiceKey[API]) = RainSensorActorImpl(pos, period, threshold, simPred, iterator, sensorsServiceKey, stationServiceKey)

  /** To simulate a sensor that measure increasing values **/
  def simulationIncrement(inc: Double): (Double, Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() + inc else n + inc

  def simulationIncrementAndReset(inc: Double): (Double, Option[Iterator[Double]]) => Double =
    var num: Double = 0
    (n, it) =>
      if it.nonEmpty then num = n + it.get.next() + inc else num = n + inc
      if num >= 50 then num = num / 10
    num
  
  /** To simulate a sensor that measure oscillating values, likely it does not surpass the threshold **/
  def simulationOscillation(rand: Random, num: Int): (Double, Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() * rand.nextInt(num).toDouble else n + rand.nextInt(num)
  
  class RainSensorActorImpl(pos: P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, it: Option[Iterator[Double]], sensorsServiceKey: ServiceKey[API], stationServiceKey: ServiceKey[API]) extends RainSensorActor:
      def createRainSensorBehavior(start: Double): Behavior[API | Receptionist.Listing] =
        Behaviors.setup[API | Receptionist.Listing] { ctx =>

          /**
           * Anonymous actor in charge of receiving notify messages telling if a sensor/station actor has spawned or died.
           * It is necessary due to akka bug that prevents an actor to register and subscribe at the same time.
           */
          ctx.spawnAnonymous[Receptionist.Listing] {
            Behaviors.setup { internal =>
              internal.system.receptionist ! Receptionist.Subscribe(sensorsServiceKey, internal.self)
              internal.system.receptionist ! Receptionist.Subscribe(stationServiceKey, internal.self)
              Behaviors.receiveMessage {
                case msg: Receptionist.Listing =>
                  ctx.self ! msg
                  Behaviors.same
              }
            }
          }
          var station: Option[ActorRef[API]] = None
          var others: Set[ActorRef[API]] = Set()
          var data = Data()
          var count = 0

          /** Timers are needed to simulate a periodic measurement**/
          Behaviors.withTimers { timers =>
            timers.startSingleTimer(Measure(simPred(0, it)), period)
            Behaviors.receiveMessage {
              // if the sensor is ok and it surpass the threshold
              // it notifies the update and tells itself and the others to take a decision
              case Measure(l) if data.state == SAMPLING && l > THRESHOLD =>
                println("THRESHOLD "+l)
                for o <- others do o ! Decide(l, ctx.self)
                data = data.copy(lastValue = l, state = LOCAL_ALARM)
                timers.startSingleTimer(Measure(simPred(l, it)), period)
                Behaviors.same
              // if the sensor is in local alarm state and it returns under the threshold
              // it notifies the update and tells itself and the others to take a decision
              case Measure(l) if data.state == LOCAL_ALARM && l <= THRESHOLD =>
                println("UNDER THRESHOLD "+l)
                for o <- others do o ! Decide(l, ctx.self)
                data = data.copy(lastValue = l, state = SAMPLING)
                timers.startSingleTimer(Measure(simPred(l, it)), period)
                Behaviors.same
              // it keeps going
              case Measure(l) =>
                println("MISURA"+l)
                data = data.copy(lastValue = l)
                timers.startSingleTimer(Measure(simPred(l, it)), period)
                Behaviors.same
              // to take a decision it saves the count of sensors that have surpassed the threshold
              case Decide(v,_) =>
                // it updates count according to the value received
                if v > threshold then count = count + 1 else count = count - 1
                // majority is reached, it notifies the station and the sensors
                if count > others.size / 2 then
                  count = 0
                  station.get ! Alarm(data.values)
                  for o <- others do o ! Alarm(Seq())
                Behaviors.same
              // it means the alarm is global
              case Alarm(_) =>
                data = data.copy(state = GLOBAL_ALARM)
                Behaviors.same
              // it means the station is solving the problem so the sensor state changes
              case Msg("SOLVING") =>
                data = data.copy(state = SOLVING)
                println("DATA" + data)
                Behaviors.same
              // it means the station has solved the problem so the sensor state changes
              // It restarts from the beginning for simulation purposes
              case Msg("SAMPLING") =>
                println("RESTART")
                timers.cancelAll()
                data = Data()
                timers.startSingleTimer(Measure(simPred(0, it)), period)
                Behaviors.same
              // if a sensor spawns or dies it updates its local list and starts a decision
              // Ex. We have two sensors and a sensor (under the threshold) dies when another sensor is in local alarm it notifies the alarm and it
              // prevents blocking
              case sensorsServiceKey.Listing(listing) if others.size != listing.size =>
                others = listing
                println("CHANGE")
                println(count)
                println(others.size / 2)
                if (count > others.size / 2)
                  println("GLOBAL ALARM, THE NUMBER OF SENSORS HAS CHANGED")
                  station.get ! Alarm(data.values)
                  count = 0
                  for o <- others do o ! Alarm(Seq())
                Behaviors.same
              // if the station spawns or dies it updates its local station
              case stationServiceKey.Listing(listing) if listing.nonEmpty =>
                station = Some(listing.head)
                Behaviors.same
              // it discards the other messages
              case _ =>
                Behaviors.same

            }

          }

    }
