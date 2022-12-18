package it.unibo.pcd.akka.basics.pt1

object CustomException:
  case class NullVectorException() extends Exception
  case class InfiniteForceException() extends Exception

