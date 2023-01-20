package assignment03.pt2

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API
import assignment03.pt2.API.*
import assignment03.pt2.API.STATE
import assignment03.pt2.API.STATE.*
import assignment03.pt2.Root.{HubServiceKey, StatsServiceKey}

import concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

trait RainSensorActor

object RainSensorActor:

  def apply(pos:P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, iterator: Option[Iterator[Double]]) = RainSensorActorImpl(pos, period, threshold, simPred, iterator)

  def simulationIncrement(inc: Double): (Double, Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() + inc else n + inc

  def simulationOscillation(rand: Random, num: Int): (Double,  Option[Iterator[Double]]) => Double =
    (n, it) => if it.nonEmpty then n + it.get.next() * rand.nextInt(num).toDouble else n + rand.nextInt(num)
  //TODO ATTORI POSSONO AVERE TUTTO QUESTO STATO ????
  class RainSensorActorImpl(pos: P2d, period: FiniteDuration, threshold: Double, simPred: (Double, Option[Iterator[Double]]) => Double, it: Option[Iterator[Double]]) extends RainSensorActor:
      def createRainSensorBehavior(start: Double): Behavior[API | Receptionist.Listing] =
        Behaviors.setup[API | Receptionist.Listing] { ctx =>

          ctx.spawnAnonymous[Receptionist.Listing] {
            Behaviors.setup { internal =>
              internal.system.receptionist ! Receptionist.Subscribe(StatsServiceKey, internal.self)
              internal.system.receptionist ! Receptionist.Subscribe(HubServiceKey, internal.self)
              Behaviors.receiveMessage {
                case msg: Receptionist.Listing =>
                  ctx.self ! msg
                  Behaviors.same
              }
            }
          }
          var hub: Option[ActorRef[API]] = None
          var others: Set[ActorRef[API]] = Set()

          var data = Data()
          Behaviors.withTimers { timers =>
            timers.startSingleTimer(Start(0, SAMPLING), period)
            Behaviors.receiveMessage {
              case Start(v, state) =>
                println("START, STATE " + data)
                data = Data().copy(lastValue = v, state = state)
                timers.startSingleTimer(Measure(simPred(v, it)), period)
                Behaviors.same
              case Measure(l) if data.state == SAMPLING && l > THRESHOLD =>
                println("THRESHOLD " + l)
                data = data.copy(lastValue = l)
                for o <- others do o ! Decide(l, ctx.self)
                Behaviors.same
              case Measure(l) if l > THRESHOLD =>
                 println("STATO MISURA SOPRA SOGLIA " + l)
                 data = data.copy(lastValue = l)
                 timers.startSingleTimer(Measure(simPred(l, it)), period)
                 Behaviors.same
              case Measure(l) =>
                data = data.copy(lastValue = l) // TODO ?
                println("STATO MISURA SOTTO SOGLIA " + l)
                timers.startSingleTimer(Measure(simPred(l, it)), period)
                Behaviors.same
              case Decide(v, ref) =>
                println("DATA" + data)
                println("SONO "+ ctx.self + "RICEVO "+ v + " DA "+ ref)
                data = data.copy(values = v +: data.values)
                println("VALORI "+data.values)
                if (data.values.count(v => v > THRESHOLD) > others.size / 2)
                  println("ALLARME GLOBALE")
                  if (hub.nonEmpty)
                    hub.get ! Alarm(data.values)
                  data = data.copy(state = ALARM)
                else
                  if (ref == ctx.self)
                    println("ALLARME LOCALE")
                    data = data.copy(state = ALARM)
                    timers.startSingleTimer(Measure(simPred(data.lastValue, it)), period)
               Behaviors.same
              case Msg("SOLVING") =>
                println("DATA" + data)
                println("SOLVING")
                data = data.copy(state = SOLVING)
                timers.cancelAll()
                timers.startSingleTimer(Measure(simPred(data.lastValue, it)), period)
                //ctx.self ! Measure(data.lastValue)
                Behaviors.same
              case Msg("OK") =>
                println("DATA" + data)
                println("OK")
                timers.cancelAll()
                ctx.self ! Start(0, SAMPLING) // TODO
                Behaviors.same
              case StatsServiceKey.Listing(listing) if others.size != listing.size =>
                others = listing
                println("MSG "+ listing)
                Behaviors.same
              case HubServiceKey.Listing(listing) if listing.nonEmpty =>
                hub = Some(listing.head)
                Behaviors.same
              case _ =>
                Behaviors.same


           

            }

          }

    }
