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
            timers.startSingleTimer(Measure(0), period)
            // TODO ANCHE SE DA SOLO DEVE POTER PRENDERE UNA DECISIONE
            Behaviors.receiveMessage {
              case Start(v) =>
                println("START")
                data = Data().copy(lastValue = v)
                timers.startSingleTimer(Measure(simPred(v,it)), period)
                Behaviors.same
              case Measure(l) if data.state == SAMPLING && l > THRESHOLD =>
                if (others.size == 1 && hub.nonEmpty)
                    println("IIIIIIIIIIIIIIIIIIIIIIFFFFFFFFFFFFFFFFFFF")
                    data = data.copy(state = ALARM)
                    hub.get ! Alarm("aiuto")
                    //ctx.self ! Start(0) // TODO dovrebbe ripartire da 0 (solo per simulare) però è rotto
                else
                  println("ELSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE")
                  data = data.copy(lastValue = l)
                  ctx.self ! StartDecision()
                Behaviors.same
              case Measure(l) if data.state == SAMPLING =>
                 data = data.copy(lastValue = l)
                 println("MISURA "+l)
                 timers.startSingleTimer(Measure(simPred(l, it)), period)
                 Behaviors.same
              case StartDecision() if data.state != UNDECIDED =>
                println("START DECISION")
                data = data.copy(state = UNDECIDED)
                for o <- others if o != ctx.self do o ! StartDecision()
                for o <- others if o != ctx.self do o ! Decide(data.values, ctx.self)
                Behaviors.same
              case Decide(v, ref) =>
                 data = data.copy(values = v.union(data.values).union(Set(data.lastValue)))
                 ref ! Deciding(data.values)
                 println("MANDO A "+ref+" "+ data.values)
                 Behaviors.same
              case Deciding(v) =>
                //Thread.sleep(1000) // TEST
                data = data.copy(received = data.received + 1, values = v.union(data.values))
                println("RISPOSTA DECIDING "+ data.values)
                if (data.received == others.size - 1)
                  data = data.copy(received = 0, alreadySent = data.values.diff(data.alreadySent), k = data.k + 1)
                  if (data.k > others.size)
                    println("k"+data.k)
                    data = data.copy(k = 0)
                    if (data.values.count(v => v > THRESHOLD) > others.size / 2)
                      data = data.copy(state = ALARM, values = Set(), alreadySent = Set())
                      //ctx.self ! Start(0) // TODO dovrebbe ripartire da 0 (solo per simulare) però è rotto
                      if (hub.nonEmpty)
                        hub.get ! Alarm("Aiuto")
                    else
                      ctx.self ! Start(data.lastValue)
                  else
                    // iniziate un altro turno
                    for o <- others if o != ctx.self do o ! Decide(data.alreadySent, ctx.self)
                Behaviors.same
              case StatsServiceKey.Listing(listing) if others.size != listing.size =>
                others = listing
                println("MSG "+ listing)
                if (others.size == 1 && data.state == UNDECIDED)
                  println("TEEEEEEEEEEEEEEEEEESTTTTTTTT")
                  ctx.self ! Start(data.lastValue)
                Behaviors.same
              case HubServiceKey.Listing(listing) if listing.nonEmpty =>

                hub = Some(listing.head)
                Behaviors.same
              case _ =>
                Behaviors.same


           

            }

          }

    }
