package assignment03.pt1.main

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import assignment03.pt1.main.API.API
import assignment03.pt1.main.Body.Body
import assignment03.pt1.main.Boundary
import assignment03.pt1.main.Utils.{POS, START, VEL}

object ReceiveBehaviour:
  case class IterationData(DT: Double, arrived: Int = 0, vt: Double = 0, currentIteration: Int = 0)

  class ReceiveBehaviour(bounds: Boundary, actors: Seq[ActorRef[API.Msg]], val initialBodies: Array[Body], viewer: ActorRef[API.UpdateGUI], N_ITERATIONS: Int, ctx: ActorContext[API]):
    val DT = 0.001
    val N_ACTORS: Int = actors.size

    def behaviourReceive(): Behaviors.Receive[API] =
      var data = IterationData(DT)
      var bodies = initialBodies
      var startRequest = false
      var behaviour: Behavior[API] = Behaviors.same

      for a <- actors do a ! API.Msg(VEL, bodies, ctx.self)

      Behaviors.receiveMessage {
        case API.Msg(START, _, _) =>
          for a <- actors do a ! API.Msg(VEL, bodies, ctx.self)
          behaviour
        case API.Messaged(VEL, bodiesToUpdate, msg) =>
          bodies = updateBodies(bodies, bodiesToUpdate)
          data = data.copy(DT = DT, arrived = data.arrived + 1)
          if (data.arrived == N_ACTORS)
            data = data.copy(DT = DT, arrived = 0)
            if startRequest then {startRequest = false; behaviour = behaviourReceive()}
            else
              for a <- actors do a ! API.Msg(POS, bodies, ctx.self)
          behaviour
        case API.Messaged(POS, bodiesToUpdate, msg) =>
          bodies = updateBodies(bodies, bodiesToUpdate)
          data = data.copy(DT = DT, arrived = data.arrived + 1)
          if (data.arrived == N_ACTORS)
            data = data.copy(DT = DT, arrived = 0, vt = data.vt + data.DT, currentIteration = data.currentIteration + 1)
            if (data.currentIteration < N_ITERATIONS)
              if startRequest then {startRequest = false; behaviour = behaviourReceive()}
              else
                viewer ! API.UpdateGUI(data.vt, data.currentIteration, bodies, bounds, ctx.self)
            else Behaviors.stopped
          behaviour
        case API.Stop =>
          Behaviors.setup[API](ctx =>
            Behaviors.receiveMessage {
              case API.Start => behaviourReceive()
              case _ => behaviour
            }
          )
        case API.Start =>
          if data.currentIteration == N_ITERATIONS then behaviour = behaviourReceive()
          startRequest = true
          behaviour


      }

    def updateBodies(bodies: Array[Body], bodiesToUpdate: Array[Body]): Array[Body] =
      bodies map { b => if bodiesToUpdate.contains(b) then bodiesToUpdate.find(b1 => b1.id == b.id).get else b}
