package assignment03.pt1

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.Body.Body
import assignment03.pt1.ProvaBody.API
import assignment03.pt1.Utils.createBodies

object BodyMain extends App:
  val N_BODY: Int = 10
  val N_ITERATIONS: Int = 10_000
  val dim: Int = 3

  val DT: Double = 0.001
  var currentIteration: Int = 0
  val bounds: Boundary = Boundary(-dim, -dim, dim, dim)
  var bodies: Seq[Body] = createBodies(N_BODY, bounds)
  bodies(0).updatePos(200000)
