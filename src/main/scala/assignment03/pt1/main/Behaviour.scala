package assignment03.pt1.main

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import assignment03.pt1.main.API.API
import assignment03.pt1.main.Body.Body
import assignment03.pt1.main.Boundary

object Behaviour:
  case class IterationData(DT: Double, arrived: Int = 0, vt: Double = 0, currentIteration: Int = 0)

  class ReceiveBehaviour(bounds: Boundary, actors: Seq[ActorRef[API.Msg]], val initialBodies: Seq[Body], viewer: ActorRef[API.UpdateGUI], N_ITERATIONS: Int, ctx: ActorContext[API]):
    val DT = 0.001
    val N_ACTORS: Int = actors.size

    def behaviourReceive(): Behaviors.Receive[API] =
      var data = IterationData(DT)
      var bodies = initialBodies
      Behaviors.receiveMessage {
        case API.Msg("Inizio", _, _) =>
          for a <- actors do a ! API.Msg("Velocity", bodies, ctx.self)
          Behaviors.same
        case API.Messaged("Velocity", bodiesToUpdate, msg) =>
          bodies = updateBodies(bodies, bodiesToUpdate)
          data = data.copy(DT = DT, arrived = data.arrived + 1)
          if (data.arrived == N_ACTORS)
            data = data.copy(DT = DT, arrived = 0)
            for a <- actors do a ! API.Msg("Position", bodies, ctx.self)
          Behaviors.same
        case API.Messaged("Position", bodiesToUpdate, msg) =>
          bodies = updateBodies(bodies, bodiesToUpdate)
          data = data.copy(DT = DT, arrived = data.arrived + 1)
          if (data.arrived == N_ACTORS)
            data = data.copy(DT = DT, arrived = 0, vt = data.vt + data.DT, currentIteration = data.currentIteration + 1)
            if (data.currentIteration < N_ITERATIONS)
              viewer ! API.UpdateGUI(data.vt, data.currentIteration, bodies, bounds, ctx.self)
            else Behaviors.stopped
          Behaviors.same
        case API.Stop =>
          Behaviors.setup[API](ctx =>
            Behaviors.receiveMessage {
              case API.Start() =>
                for a <- actors do a ! API.Msg("Velocity", initialBodies, ctx.self)
                behaviourReceive()
              case _ => Behaviors.same
            }
          )
        case API.Start() =>
          // TODO bug
          for a <- actors do a ! API.Msg("Velocity", initialBodies, ctx.self)
          behaviourReceive()

      }

    def updateBodies(bodies: Seq[Body], bodiesToUpdate: Seq[Body]): Seq[Body] =
      bodies map { b => if bodiesToUpdate.contains(b) then bodiesToUpdate.find(b1 => b1.id == b.id).get else b}
      //bodies map { b => if b.id >= start && b.id < end then bodiesToUpdate.filter(body => body.id == b.id).head else b }
