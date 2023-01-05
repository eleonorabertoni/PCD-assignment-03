package assignment03.pt2

import akka.actor.typed.ActorRef

object API:
  
  enum API:
    case Measure(rainLevel: Double)
    case Deciding(actorsValues: Set[Double])
    case Alarm(msg: String)
    case Start(start: Double, from: ActorRef[API])
    case Notify(rainLevel: Double, from: ActorRef[API])

  enum STATE:
    case SAMPLING, UNDECIDED, DECIDED, ALARM
