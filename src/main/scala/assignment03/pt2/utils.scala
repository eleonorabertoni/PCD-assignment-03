package assignment03.pt2

import akka.actor.typed.{ActorSystem, Behavior}
import com.typesafe.config.ConfigFactory
import scala.util.{Random, Success}
import concurrent.duration.DurationInt

val N_RAIN_SENSOR: Int = 4
val N_RAIN_OSCILLATOR: Int = 1
val WIDTH: Int = 3
val HEIGHT: Int = 3
val rand: Random = Random(999)
val PERIOD = 2000.millis
val THRESHOLD = 35
val seeds = List(2551, 2552) // seed used in the configuration

def startupWithRole[X](role: String, port: Int)(root: => Behavior[X]): ActorSystem[X] =
  val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """)
    .withFallback(ConfigFactory.load("base-cluster"))

  // Create an Akka system
  ActorSystem(root, "ClusterSystem", config)
