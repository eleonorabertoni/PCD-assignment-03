package assignment03.pt2

import akka.actor.typed.ActorRef
import assignment03.pt1.main.Boundary

object API:

  trait API
  case class Measure(rainLevel: Double) extends API with Message
  case class Decide(value: Double, ref: ActorRef[API]) extends API with Message
  case class Alarm(values: Seq[Double]) extends API with Message
  case class Start(v: Double, state: STATE) extends API with Message
  case class MsgSensor(n: Int) extends API with Message
  case class Msg(info: String) extends API with Message
  case class Stop() extends API with Message

  enum STATE:
    case SAMPLING, ALARM, SOLVING
  
  enum HUB_STATE:
    case FREE, OCCUPIED
