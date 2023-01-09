package assignment03.pt2

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.P2d.P2d
import assignment03.pt2.API
import assignment03.pt2.API.*
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
  //TODO ATTORI POSSONO AVERE TUTTO QUESTO STATO ????
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
          //var mapActorsValues: Map[ActorRef[API], Double] = Map()
          //var alreadySent: Map[ActorRef[API], Double] = Map()
          var values: Set[Double] = Set()
          var alreadySent: Set[Double] = Set()
          var lastValue: Double = -1
          var state: STATE = SAMPLING
          var k = 0
          var received = 0
          Behaviors.withTimers { timers =>
            timers.startSingleTimer(Measure(0), period)
            // TODO CONSENSUS E SPERO NON SI BLOCCHINO
            Behaviors.receiveMessage {
              case Start() =>
                println("START")
                state = SAMPLING
                values = Set()
                alreadySent = Set()
                timers.startSingleTimer(Measure(simPred(lastValue,it)), period)
                Behaviors.same
              case Measure(l) if state == SAMPLING && l > THRESHOLD =>
                lastValue = l
                println("MISURA THRESHOLD"+ l)
                // TODO INIZIA IL PROCESSO DI DECISIONE ?
                state = UNDECIDED
                //values = values + (ctx.self -> l)
                // TODO
                values = values + l
                alreadySent = values
                for o <- others if o != ctx.self do o ! Decide(values, ctx.self)
                Behaviors.same
              case Measure(l) if state == SAMPLING =>
                 lastValue = l
                 println("MISURA "+l)
                 timers.startSingleTimer(Measure(simPred(l, it)), period)
                 Behaviors.same
              case Decide(v, ref) =>
                 state = UNDECIDED
                 values = v.union(values).union(Set(lastValue))
                 ref ! Deciding(values)
                 println("RICHIESTA DECIDE "+ctx.self+" "+ values)
                 Behaviors.same
              case Deciding(v) =>
                received = received + 1
                values = v.union(values)
                println("RISPOSTA DECIDING "+ctx.self+" "+ values)
                if (received == others.size - 1)
                  println("1 if")
                  received = 0
                  alreadySent = values.diff(alreadySent)
                  k = k + 1
                  if (k > others.size)
                    println("k"+k)
                    k = 0
                    println("2 if")
                    if (values.count(v => v > THRESHOLD) > others.size / 2)
                      println("3if")
                      state = ALARM
                      values = Set()
                      alreadySent = Set()
                      for o <- others if o != ctx.self do o ! Alarm("Aiuto")
                    else
                      println("else")
                      state = UNDECIDED
                      for o <- others do o ! Start()
                  else
                    // iniziate un altro turno
                    for o <- others if o != ctx.self do o ! Decide(alreadySent, ctx.self)
                Behaviors.same
              case Alarm(msg) =>
                state = ALARM
                println("ALARM "+ctx.self)
                Behaviors.same


              case StatsServiceKey.Listing(listing) if others.size != listing.size =>
                others = listing
                println("MSG "+ listing)
                Behaviors.same
              case _ => Behaviors.same


           

            }

          }

    }
