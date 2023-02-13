package assignment03.pt1.main.noGUIMain

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import assignment03.pt1.main.API.API
import assignment03.pt1.main.Body.Body
import assignment03.pt1.main.Boundary
import assignment03.pt1.main.Utils.{POS, START, STOP, VEL}

object ReceiveBehaviourNoGUI:
  case class IterationData(DT: Double, arrived: Int = 0, vt: Double = 0, currentIteration: Int = 0)

  class ReceiveBehaviourNoGUI(bounds: Boundary, actors: Seq[ActorRef[API.Msg]], val initialBodies: Array[Body], N_ITERATIONS: Int, ctx: ActorContext[API]):
    val DT = 0.001
    val N_ACTORS: Int = actors.size

    def behaviourReceive(): Behaviors.Receive[API] =
      val t0 = System.currentTimeMillis()
      ctx.log.info(s"$t0")
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
            if startRequest then {
              startRequest = false; behaviour = behaviourReceive()
            }
            else
              for a <- actors do a ! API.Msg(POS, bodies, ctx.self)
          behaviour
        case API.Messaged(POS, bodiesToUpdate, msg) =>
          bodies = updateBodies(bodies, bodiesToUpdate)
          data = data.copy(DT = DT, arrived = data.arrived + 1)
          if (data.arrived == N_ACTORS)
            data = data.copy(DT = DT, arrived = 0, vt = data.vt + data.DT, currentIteration = data.currentIteration + 1)
            if (data.currentIteration < N_ITERATIONS)
              if startRequest then {
                startRequest = false; behaviour = behaviourReceive()
              }
              else
                for a <- actors do a ! API.Msg(VEL, bodies, ctx.self)
            else
              for a <- actors do a ! API.Msg(STOP, Array(), ctx.self)
              val t1 = System.currentTimeMillis()
              val elapsed = t1 - t0
              ctx.log.info(s"$t1")
              ctx.log.info(s"elapsed $elapsed")
              Behaviors.stopped
              println("fine")
          behaviour
      }

    def updateBodies(bodies: Array[Body], bodiesToUpdate: Array[Body]): Array[Body] =
      bodies map { b => if bodiesToUpdate.contains(b) then bodiesToUpdate.find(b1 => b1.id == b.id).get else b }
