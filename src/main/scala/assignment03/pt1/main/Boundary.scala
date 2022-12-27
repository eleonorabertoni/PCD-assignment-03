package assignment03.pt1.main

trait Boundary:
  def x0: Int
  def y0: Int
  def x1: Int
  def y1: Int

object Boundary:
  def apply(x0: Int, y0: Int, x1: Int, y1: Int): Boundary = BoundaryImpl(x0, y0, x1, y1)

case class BoundaryImpl(x0:Int, y0:Int, x1: Int, y1:Int) extends Boundary
