package assignment03.pt2

import assignment03.pt2.API.STATE
import assignment03.pt2.API.STATE.*

object Data:
  def apply() = DataImpl()
  def apply(values: Seq[Double], lastValue: Double, state: STATE, k: Int, received: Int) = DataImpl(values, lastValue, state, k, received)
  case class DataImpl(values: Seq[Double] = Seq(), lastValue:Double = -1, state:STATE = SAMPLING, k: Int = 0, received: Int = 0)
