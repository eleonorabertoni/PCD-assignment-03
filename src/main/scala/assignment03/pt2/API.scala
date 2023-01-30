package assignment03.pt2

import akka.actor.typed.ActorRef
import assignment03.pt1.main.Boundary

object API:

  trait API
  case class Measure(rainLevel: Double) extends API with Message
  case class Decide(value: Double, ref: ActorRef[API]) extends API with Message
  case class Alarm(values: Seq[Double]) extends API with Message
  case class MsgSensor(n: Int) extends API with Message // to send the view the number of sensors
  case class Msg(info: String) extends API with Message // to send the station state or the zone state
  case class Stop() extends API with Message // to disable the alarm in a zone

  /** Zone and Sensor state **/
  enum STATE:
    case SAMPLING, ALARM, SOLVING
  
  /** Fire Station state **/
  enum STATION_STATE:
    case FREE, OCCUPIED
