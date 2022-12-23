package assignment03.pt1

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import assignment03.pt1.Body.Body
import assignment03.pt1.ProvaBehaviour.API

object Behaviour:
  def behaviourReceive(bounds: Boundary, actors: Seq[ActorRef[API.Msg]], bodiesB:Seq[Body], viewer: ActorRef[API.UpdateGUI], N_ACTORS: Int, N_ITERATIONS: Int, ctx: ActorContext[API]): Behaviors.Receive[API] =
    var arrivati: Int = 0
    var vt: Double = 0
    var currentIteration: Int = 0
    val DT: Double = 0.001
    println(bodiesB)
    println(" ")
    var bodies = Seq.from(bodiesB)
    Behaviors.receiveMessage{
      case API.Msg("Inizio", _, _) =>
        for a <- actors do a ! API.Msg("Velocity", bodies.toList, ctx.self)
        Behaviors.same
      case API.Messaged("Velocity", start, end, bodiesToUpdate, msg) =>
        // TODO HO FINITO VEL
        bodies = bodies map {b => if b.id >= start && b.id < end then bodiesToUpdate.filter(body => body.id == b.id).head else b} // TODO ???
        arrivati = arrivati + 1
        if(arrivati == N_ACTORS)
          arrivati = 0
          for a <- actors do a ! API.Msg("Position", bodies.toList, ctx.self)
        Behaviors.same
      case API.Messaged("Position", start, end, bodiesToUpdate, msg) =>
        bodies = bodies map {b => if b.id >= start && b.id < end then bodiesToUpdate.filter(body => body.id == b.id).head else b}
        arrivati = arrivati + 1
        if(arrivati == N_ACTORS)
          arrivati = 0
          vt = vt + DT
          currentIteration = currentIteration + 1
          if (currentIteration < N_ITERATIONS)
          //println(currentIteration)
            viewer ! API.UpdateGUI(vt, currentIteration, bodies.toList, bounds, ctx.self)
          //for a <- actors do a ! API.Msg("Velocity", bodies.toList, ctx.self) // TODO ???
          else Behaviors.stopped
        // TODO UCCIDI I FIGLI PERCHè IL PROGRAMMA NON SE FERMA DA SOLO
        Behaviors.same
      case API.Stop =>
        Behaviors.setup[API](ctx =>
          Behaviors.receiveMessage{
            case API.Start() =>
              // TODO START NON RIPRENDE A CAPO
              for a <- actors do a ! API.Msg("Velocity", bodiesB, ctx.self)
              behaviourReceive(bounds, actors, bodiesB, viewer, N_ACTORS, N_ITERATIONS, ctx)
            case _ => Behaviors.same
          }
        )
      case API.Start() =>
        // TODO START NON RIPRENDE A CAPO
        for a <- actors do a ! API.Msg("Velocity", bodiesB, ctx.self)
        behaviourReceive(bounds, actors, bodiesB, viewer, N_ACTORS, N_ITERATIONS, ctx)

    }

