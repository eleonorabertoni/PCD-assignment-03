package assignment03.pt1.pt1

import V2d.*

object P2d:

  trait P2d:
    def sum(v: V2d): P2d
    def change(x: Double, y: Double): Unit
    def x: Double
    def y: Double

  private case class P2dImpl(var x: Double, var y: Double) extends P2d:

    def sum(v: V2d): P2d= P2d(v.x, v.y)

    def change(x1: Double, y1: Double): Unit =
      x = x1
      y = y1

  def apply(x: Double, y: Double): P2d = P2dImpl(x, y)

