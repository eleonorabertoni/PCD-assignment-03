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
  // ABBIAMO UCCISO I TIMER QUINDI ORA IL MESSAGGIO "IN PIU" MUORE COL TIMER
  // QUINDI FORSE POSSIAMO RIPENSARE LA STRUTTURA E TOGLIERE RESTARTING
  // FORSE POSSIAMO FARLI PARTIRE ANCHE DOPO CHE RAGGIUNGONO LA THRESHOLD
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
          // TODO PROBLEMA NON RIPRENDE AD AVVERTIRE !!!
          Behaviors.withTimers { timers =>
            timers.startSingleTimer(Start(0, SAMPLING), period)
            Behaviors.receiveMessage {
              case Start(v, state) =>
                println("START, STATE 1" + data)
                //Thread.sleep(2000) //test
                data = Data().copy(lastValue = v, state = state)
                println("START, STATE 2" + data)
                timers.startSingleTimer(Measure(simPred(v, it)), period)
                Behaviors.same
              case Measure(l) if data.state == SAMPLING && l > THRESHOLD =>
                println("DATA" + data)
                println("THRESHOLD " + l)
                data = data.copy(lastValue = l)
                for o <- others do o ! Decide(l, ctx.self)
                Behaviors.same
              //case Measure(_) if data.state == RESTARTING => Behaviors.same
              case Measure(l) if l > THRESHOLD =>
                 //if data.state == RESTARTING then data = data.copy(state = SAMPLING)
                 data = data.copy(lastValue = l)
                 if data.state != RESTARTING then
                   println("DATA" + data)
                   println("MISURA "+l)
                   timers.startSingleTimer(Measure(simPred(l, it)), period)
                 //
                 Behaviors.same
              case Measure(l) =>
                data = data.copy(lastValue = l) // TODO ?
                println("DATA" + data)
                println("STATO MISURA SOTTO SOGLIA "+data.state)
                println("MISURA "+l)
                if data.state == RESTARTING then data = data.copy(state = SAMPLING)
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
                  //ctx.self ! Start(data.lastValue, ALARM)
                  data = data.copy(state = ALARM)
                  //ctx.self ! Measure(data.lastValue)
                else
                  if (ref == ctx.self)
                    println("ALLARME LOCALE")
                    data = data.copy(state = ALARM)
               Behaviors.same
              case Msg("SOLVING") =>
                println("DATA" + data)
                println("SOLVING")
                data = data.copy(state = SOLVING)
                timers.cancelAll()
                ctx.self ! Measure(data.lastValue)
                Behaviors.same
              case Msg("OK") =>
                println("DATA" + data)
                println("OK")
                timers.cancelAll()
                ctx.self ! Start(0, RESTARTING) // TODO
                Behaviors.same


              case StatsServiceKey.Listing(listing) if others.size != listing.size =>
                others = listing
                println("MSG "+ listing)
                /*
                if (others.size == 1 && data.state == UNDECIDED)
                  println("TEEEEEEEEEEEEEEEEEESTTTTTTTT")
                  ctx.self ! Start(data.lastValue, SAMPLING)
                */
                Behaviors.same
              case HubServiceKey.Listing(listing) if listing.nonEmpty =>
                hub = Some(listing.head)
                Behaviors.same
              case _ =>
                Behaviors.same


           

            }

          }

    }
