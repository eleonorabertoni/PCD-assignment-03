package assignment03.pt1

object CustomException:
  case class NullVectorException() extends Exception
  case class InfiniteForceException() extends Exception

