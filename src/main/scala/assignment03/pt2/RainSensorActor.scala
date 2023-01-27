package assignment03.pt2

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API
import assignment03.pt2.API.*
import assignment03.pt2.API.STATE
import assignment03.pt2.API.STATE.*

import concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

trait RainSensorActor

object RainSensorActor:

  def apply(pos:P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, iterator: Option[Iterator[Double]], sensorsServiceKey: ServiceKey[API], hubServiceKey: ServiceKey[API]) = RainSensorActorImpl(pos, period, threshold, simPred, iterator, sensorsServiceKey, hubServiceKey)

  /** To simulate a sensor that measure increasing values **/
  def simulationIncrement(inc: Double): (Double, Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() + inc else n + inc
  
  /** To simulate a sensor that measure oscillating values, likely it does not surpass the threshold **/
  def simulationOscillation(rand: Random, num: Int): (Double, Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() * rand.nextInt(num).toDouble else n + rand.nextInt(num)
  
  class RainSensorActorImpl(pos: P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, it: Option[Iterator[Double]], sensorsServiceKey: ServiceKey[API], hubServiceKey: ServiceKey[API]) extends RainSensorActor:
      def createRainSensorBehavior(start: Double): Behavior[API | Receptionist.Listing] =
        Behaviors.setup[API | Receptionist.Listing] { ctx =>

          /**
           * Anonymous actor in charge of receiving notify messages telling if a sensor/station actor has spawned or died.
           * It is necessary due to akka bug that prevents an actor to register and subscribe at the same time.
           */
          ctx.spawnAnonymous[Receptionist.Listing] {
            Behaviors.setup { internal =>
              internal.system.receptionist ! Receptionist.Subscribe(sensorsServiceKey, internal.self)
              internal.system.receptionist ! Receptionist.Subscribe(hubServiceKey, internal.self)
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
          
          /** Timers are needed to simulate a periodic measurement**/
          Behaviors.withTimers { timers =>
            timers.startSingleTimer(Start(0, SAMPLING), period)
            Behaviors.receiveMessage {
              // to restart the sensor from the beginning
              case Start(v, state) =>
                data = Data().copy(lastValue = v, state = state)
                timers.startSingleTimer(Measure(simPred(v, it)), period)
                Behaviors.same
              // if the sensor is not in alarm state and it surpass the threshold 
              // it tells itself and the others to take a decision
              case Measure(l) if data.state == SAMPLING && l > THRESHOLD =>
                println("THRESHOLD " + l)
                data = data.copy(lastValue = l)
                for o <- others do o ! Decide(l, ctx.self)
                Behaviors.same
              // it keeps going 
              case Measure(l) =>
                 if l > THRESHOLD then println("STILL THRESHOLD " + l) else println("OK " + l)
                 data = data.copy(lastValue = l)
                 timers.startSingleTimer(Measure(simPred(l, it)), period)
                 Behaviors.same
              // to take a decision it saves the values and calls the decision algorithm (majority)
              case Decide(v, ref) =>
                println("I am "+ ctx.self + "I received "+ v + " from "+ ref)
                data = data.copy(values = v +: data.values)
                println("My values "+data.values)
                // if the majority surpass the threshold the alarm is global, its local state is ALARM and it notifies the station
                if (data.values.count(v => v > THRESHOLD) > others.size / 2)
                  println("Global Alarm")
                  if station.nonEmpty then station.get ! Alarm(data.values)
                  data = data.copy(state = ALARM)
                // otherwise
                else
                  // if it is no the one that started the decision it does nothing
                  // if it is the one that started the decision its local state is ALARM (it has surpassed the threshold) and keeps going
                  if (ref == ctx.self)
                    println("LOCAL ALARM ")
                    data = data.copy(state = ALARM)
                    timers.startSingleTimer(Measure(simPred(data.lastValue, it)), period)
               Behaviors.same
              // it means the station is solving the problem so the sensor state changes
              case Msg("SOLVING") =>
                data = data.copy(state = SOLVING)
                timers.cancelAll()
                timers.startSingleTimer(Measure(simPred(data.lastValue, it)), period)  //TODO maybe it is useless
                println("DATA" + data)
                Behaviors.same
              // it means the station has solved the problem so the sensor state changes
              // It restarts from the beginning for simulation purposes
              case Msg("SAMPLING") =>
                println("RESTART")
                timers.cancelAll()
                ctx.self ! Start(0, SAMPLING) // TODO
                Behaviors.same
              // if a sensor spawns or dies it updates its local list and starts a decision 
              // Ex. We have two sensors and a sensor (under the threshold) dies when another sensor is in local alarm it notifies the alarm  
              case sensorsServiceKey.Listing(listing) if others.size != listing.size =>
                others = listing
                if (data.values.count(v => v > THRESHOLD) > others.size / 2)
                  println("GLOBAL ALARM, THE NUMBER OF SENSORS HAS CHANGED") //TODO 
                  if (station.nonEmpty)
                    station.get ! Alarm(data.values)
                  data = data.copy(state = ALARM)
                Behaviors.same
              // if the station spawns or dies it updates its local station
              case hubServiceKey.Listing(listing) if listing.nonEmpty =>
                station = Some(listing.head)
                Behaviors.same
              // it discards the other messages
              case _ =>
                Behaviors.same

            }

          }

    }
