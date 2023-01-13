package assignment03.pt2

import akka.actor.typed.ActorRef

object API:

  trait API
  case class Measure(rainLevel: Double) extends API with Message
  case class Decide(value: Double, ref: ActorRef[API]) extends API with Message
  case class Alarm(values: Seq[Double]) extends API with Message
  case class Start(v: Double, state: STATE) extends API with Message
  case class Msg(info: String) extends API with Message

  enum STATE:
    case SAMPLING, ALARM, SOLVING, RESTARTING
  
  enum HUB_STATE:
    case FREE, OCCUPIED
