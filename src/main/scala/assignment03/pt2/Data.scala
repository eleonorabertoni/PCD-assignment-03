package assignment03.pt2

import assignment03.pt2.API.STATE
import assignment03.pt2.API.STATE.*

object Data:
  def apply() = DataImpl()
  def apply(values: Set[Double], alreadySent: Set[Double], lastValue: Double, state: STATE, k: Int, received: Int) = DataImpl(values, alreadySent, lastValue, state, k, received)
  case class DataImpl(values: Set[Double] = Set(), alreadySent: Set[Double] = Set(), lastValue:Double = -1, state:STATE = SAMPLING, k: Int = 0, received: Int = 0)
