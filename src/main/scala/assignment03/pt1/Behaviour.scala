package assignment03.pt1

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import assignment03.pt1.Body.Body
import assignment03.pt1.ProvaBehaviour.API

object Behaviour:
  case class IterationData(DT: Double, arrived: Int = 0, vt: Double = 0, currentIteration: Int = 0)
  

  def updateBodies(bodies: Seq[Body], bodiesToUpdate: Seq[Body], start: Int, end: Int): Seq[Body] =
    bodies map {b => if b.id >= start && b.id < end then bodiesToUpdate.filter(body => body.id == b.id).head else b}

  def behaviourReceive(bounds: Boundary, actors: Seq[ActorRef[API.Msg]], bodiesB:Seq[Body], viewer: ActorRef[API.UpdateGUI], N_ACTORS: Int, N_ITERATIONS: Int, ctx: ActorContext[API]): Behaviors.Receive[API] =
    val DT = 0.001
    var data = IterationData(DT)
    println(bodiesB)
    println(" ")
    var bodies = Seq.from(bodiesB)
    Behaviors.receiveMessage{
      case API.Msg("Inizio", _, _) =>
        for a <- actors do a ! API.Msg("Velocity", bodies.toList, ctx.self)
        Behaviors.same
      case API.Messaged("Velocity", start, end, bodiesToUpdate, msg) =>
        // TODO HO FINITO VEL
        bodies = updateBodies(bodies, bodiesToUpdate, start, end) // TODO ???
        data = data.copy(DT = DT, arrived = data.arrived + 1)
        if(data.arrived == N_ACTORS)
          data = data.copy(DT = DT, arrived = 0)
          for a <- actors do a ! API.Msg("Position", bodies.toList, ctx.self)
        Behaviors.same
      case API.Messaged("Position", start, end, bodiesToUpdate, msg) =>
        bodies = updateBodies(bodies, bodiesToUpdate, start, end)
        data = data.copy(DT = DT, arrived = data.arrived + 1)
        if(data.arrived == N_ACTORS)
          data = data.copy(DT = DT, arrived = 0, vt = data.vt + data.DT, currentIteration = data.currentIteration + 1)
          if (data.currentIteration < N_ITERATIONS)
            viewer ! API.UpdateGUI(data.vt, data.currentIteration, bodies.toList, bounds, ctx.self)
          else Behaviors.stopped
        // TODO UCCIDI I FIGLI PERCHè IL PROGRAMMA NON SE FERMA DA SOLO
        Behaviors.same
      case API.Stop =>
        Behaviors.setup[API](ctx =>
          Behaviors.receiveMessage{
            case API.Start() =>
              // TODO START NON RIPRENDE A CAPO
              println("start")
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

