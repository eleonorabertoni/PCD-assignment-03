package assignment03.pt1.pt1

import assignment03.pt1.pt1.Body.Body
import assignment03.pt1.pt1.V2d.V2d
import Body.Body
import V2d.V2d

import scala.util.Random

object Utils:

  def computeTotalForceOnBody(b: Body, bodies: Seq[Body]): V2d =
    val totalForce: V2d = V2d(0,0)
    for j <- 0 until bodies.size do
      val otherBody: Body = bodies(j)
      if (!b.equals(otherBody)){
        try {
          val forceByOtherBody: V2d = b.computeRepulsiveForceBy(otherBody)
          totalForce.sum(forceByOtherBody)
        } catch{
          case ex: Exception => Exception()
        }
      }
    /* add friction force */
    totalForce.sum(b.getCurrentFrictionForce)

  def createBodies(nBodies: Int, bounds: Boundary): Seq[Body] =
    val rand: Random = Random(999)
    var bodies: Seq[Body] = List.empty
    for i <- 0 until nBodies do
      val x: Double = bounds.x0 *0.25 + rand.nextDouble() * (bounds.x1 - bounds.x0) * 0.25
      val y: Double = bounds.y0 * 0.25 + rand.nextDouble() * (bounds.y1 - bounds.y0) * 0.25
      val b: Body = Body(i, P2d(x, y), V2d(2, 2), 10)
      bodies = bodies :+ b
    bodies
