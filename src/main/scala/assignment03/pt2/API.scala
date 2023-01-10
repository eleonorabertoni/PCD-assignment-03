package assignment03.pt2

import akka.actor.typed.ActorRef

object API:

  trait API
  case class Measure(rainLevel: Double) extends API with Message
  case class Decide(actorsValues: Set[Double], ref: ActorRef[API]) extends API with Message
  case class Deciding(actorsValues: Set[Double]) extends API with Message
  case class StartDecision() extends API with Message
  case class Alarm(msg: String) extends API with Message
  case class Start(v: Double) extends API with Message
  case class Test() extends API with Message
  case class Notify(rainLevel: Double, from: ActorRef[API]) extends API with Message

  enum STATE:
    case SAMPLING, UNDECIDED, DECIDED, ALARM
