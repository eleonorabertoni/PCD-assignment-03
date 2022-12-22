package assignment03.pt1

import V2d.*

object P2d:

  trait P2d:
    def sum(v: V2d): P2d
    def change(x: Double, y: Double): P2d
    def x: Double
    def y: Double

  private case class P2dImpl(var x: Double, var y: Double) extends P2d:

    def sum(v: V2d): P2d = P2d(x + v.x, y + v.y)

    def change(x1: Double, y1: Double): P2d = P2dImpl(x1, y1)

  def apply(x: Double, y: Double): P2d = P2dImpl(x, y)

