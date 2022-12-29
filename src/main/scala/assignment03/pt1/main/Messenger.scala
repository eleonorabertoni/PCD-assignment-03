package assignment03.pt1.main

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.main.API.API
import assignment03.pt1.main.Body.Body
import assignment03.pt1.main.Utils.{POS, VEL, computeTotalForceOnBody}

object Messenger:

  def apply(start: Int, end: Int, bounds: Boundary, DT:Double): Behavior[API.Msg] = Behaviors.receive {

    case (ctx, API.Msg(VEL, bodies, repl)) =>
      var bodiesToUpdate: Array[Body] = Array()
      for i <- start until end do
        val b = bodies(i)
        val totalForce = computeTotalForceOnBody(b, bodies)
        val acc = V2d(totalForce).scalarMul(1.0 / b.mass)
        bodiesToUpdate = bodiesToUpdate :+ b.updateVelocity(acc, DT)
      repl ! API.Messaged(VEL, bodiesToUpdate, ctx.self)
      Behaviors.same

    case (ctx, API.Msg(POS, bodies, repl)) =>
      var bodiesToUpdate: Array[Body] = Array()
      for i <- start until end do
        val b = bodies(i)
        bodiesToUpdate = bodiesToUpdate :+ b.updatePos(DT).checkAndSolveBoundaryCollision(bounds)
      repl ! API.Messaged(POS, bodiesToUpdate, ctx.self)
      Behaviors.same

  }
