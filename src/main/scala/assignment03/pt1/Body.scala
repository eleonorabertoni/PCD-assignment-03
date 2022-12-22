package assignment03.pt1

import P2d.P2d
import V2d.V2d
import CustomException.InfiniteForceException
import P2d.P2d
import V2d.V2d

object Body :

  trait Body:

    def mass: Double

    def pos: P2d

    def vel: V2d

    def id: Int

    def equals(b: Any): Boolean

    def updatePos(dt: Double): Unit

    def updateVelocity(acc: V2d, dt: Double): Unit

    def changeVel(vx: Double, vy: Double): Unit

    def getDistanceFrom(b: Body): Double

    /**
     *
     * Compute the repulsive force exerted by another body
     *
     * @param b
     * @return
     * @throws InfiniteForceException
     */
    @throws[InfiniteForceException]
    def computeRepulsiveForceBy(b: Body): V2d

    /**
     *
     * Compute current friction force, given the current velocity
     */
    def getCurrentFrictionForce: V2d

    /**
     * Check if there collisions with the boundaty and update the
     * position and velocity accordingly
     *
     * @param bounds
     */
    def checkAndSolveBoundaryCollision(bounds: Boundary): Unit

  def apply(id: Int, pos: P2d, vel: V2d, mass: Double) = BodyImpl(id, pos, vel, mass)

  case class BodyImpl(id: Int, var pos: P2d, var vel: V2d, mass: Double) extends Body:
    val REPULSIVE_CONST: Double = 0.01
    val FRICTION_CONST: Double = 1

    override def equals(obj: Any): Boolean = obj match {
      case b:Body => b.id == id
    }
    /**
     * Update the position, according to current velocity
     *
     * @param dt time elapsed
     */
    def updatePos(dt: Double) = pos = pos.sum(V2d(vel).scalarMul(dt))
    /**
     * Update the velocity, given the instant acceleration
     * @param acc instant acceleration
     * @param dt time elapsed
     */
    def updateVelocity(acc: V2d, dt: Double): Unit = vel = vel.sum(V2d(acc).scalarMul(dt))
    /**
     * Change the velocity
     *
     * @param vx
     * @param vy
     */
    def changeVel(vx: Double, vy: Double) = vel = vel.change(vx, vy)
    /**
     * Computes the distance from the specified body
     *
     * @param b
     * @return
     */
    def getDistanceFrom(b: Body): Double =
      val dx = pos.x - b.pos.x
      val dy = pos.y - b.pos.y
      Math.sqrt(dx*dx + dy*dy)

    /**
     *
     * Compute the repulsive force exerted by another body
     *
     * @param b
     * @return
     * @throws InfiniteForceException
     */
    case class InfiniteForceException() extends Exception
    @throws[InfiniteForceException]
    def computeRepulsiveForceBy(b: Body): V2d =
      val dist: Double = getDistanceFrom(b)
      if dist > 0 then
        try {
          V2d(b.pos, pos).normalize().scalarMul(b.mass*REPULSIVE_CONST/(dist*dist))
        }catch {
          case e => throw InfiniteForceException()
        }
      else
        throw InfiniteForceException()
    /**
     *
     * Compute current friction force, given the current velocity
     */
    def getCurrentFrictionForce: V2d = V2d(vel).scalarMul(-FRICTION_CONST)
    /**
     * Check if there collisions with the boundaty and update the
     * position and velocity accordingly
     *
     * @param bounds
     */
    def checkAndSolveBoundaryCollision(bounds: Boundary) =
      def changePos(x: Double, y: Double) = pos = pos.change(x, y)
      val x = pos.x
      val y = pos.y

      if (x > bounds.x1){
        changePos(bounds.x1, pos.y)
        changeVel(-vel.x, vel.y)
      } else if (x < bounds.x0){
        changePos(bounds.x0, pos.y)
        changeVel(-vel.x, vel.y)
      }
      if (y > bounds.y1){
        changePos(pos.x, bounds.y1)
        changeVel(vel.x, -vel.y)
      } else if (y < bounds.y0) {
        changePos(pos.x, bounds.y0)
        changeVel(vel.x, -vel.y)
      }
      /*
      if (x > bounds.x1){
        pos.change(bounds.x1, pos.y)
        vel.change(-vel.x, vel.y)
      } else if (x < bounds.x0){
        pos.change(bounds.x0, pos.y)
        vel.change(-vel.x, vel.y);
      }

      if (y > bounds.y1){
        pos.change(pos.x, bounds.y1)
        vel.change(vel.x, -vel.y)
      } else if (y < bounds.y0){
        pos.change(pos.x, bounds.y0)
        vel.change(vel.x, -vel.y)
      }*/