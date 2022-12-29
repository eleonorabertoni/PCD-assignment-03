package assignment03.pt1.main

import assignment03.pt1.main.Body.Body
import assignment03.pt1.main.V2d.V2d
import assignment03.pt1.main.Boundary


import scala.util.Random

object Utils:

  val VEL: String = "Velocity"
  val POS: String = "Position"
  val START: String = "Start!"

  def computeTotalForceOnBody(b: Body, bodies: Array[Body]): V2d =
    var totalForce: V2d = V2d(0, 0)
    for j <- 0 until bodies.size do
      val otherBody: Body = bodies(j)
      if (!b.equals(otherBody)) {
        try {
          val forceByOtherBody: V2d = b.computeRepulsiveForceBy(otherBody)
          totalForce = totalForce.sum(forceByOtherBody)
        } catch {
          case ex: Exception => Exception()
        }
      }
    totalForce.sum(b.getCurrentFrictionForce)

  def createBodies(nBodies: Int, bounds: Boundary): Array[Body] =
    val rand: Random = Random(999)
    var bodies: Array[Body] = Array()
    for i <- 0 until nBodies do
      val x: Double = bounds.x0 * 0.25 + rand.nextDouble() * (bounds.x1 - bounds.x0) * 0.25
      val y: Double = bounds.y0 * 0.25 + rand.nextDouble() * (bounds.y1 - bounds.y0) * 0.25
      val b: Body = Body(i, P2d(x, y), V2d(0, 0), 10)
      bodies = bodies :+ b
    bodies
