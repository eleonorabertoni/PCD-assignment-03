package assignment03.pt1.main

import assignment03.pt1.main.CustomException.NullVectorException
import assignment03.pt1.main.P2d.P2d

/**
 *
 * 2-dimensional vector
 * objects are completely state-less
 *
 */
object V2d:
  trait V2d:
    def scalarMul(k: Double): V2d

    def sum(v: V2d): V2d

    def normalize(): V2d

    def change(x: Double, y: Double): V2d

    def x: Double

    def y: Double

  def apply(v: V2d) = V2dImpl(v.x, v.y)

  def apply(from: P2d, to: P2d) = V2dImpl(to.x - from.x, to.y - from.y)

  def apply(x: Double, y: Double) = V2dImpl(x, y)

  case class V2dImpl(x: Double, y: Double) extends V2d :

    def scalarMul(k: Double): V2d = V2dImpl(x * k, y * k)

    def sum(v: V2d): V2d = V2dImpl(x + v.x, y + v.y)

    @throws[NullVectorException]
    def normalize(): V2d =
      def mod: Double = Math.sqrt(x * x + y * y)

      if mod > 0 then V2dImpl(x / mod, y / mod) else throw NullVectorException()

    def change(x: Double, y: Double): V2d = V2dImpl(x, y)
