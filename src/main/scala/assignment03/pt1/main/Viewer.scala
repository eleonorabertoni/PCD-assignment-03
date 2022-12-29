package assignment03.pt1.main

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.GUI.SimulationView
import assignment03.pt1.main.API.API
import assignment03.pt1.main.Body.Body
import assignment03.pt1.main.Utils.START

import java.util

object Viewer:

  private def toJava(bodies: Array[Body]): util.ArrayList[Body] =
    val temp = util.ArrayList[Body]()
    for b <- bodies do temp.add(b)
    temp

  def apply(bodies: Array[Body], bounds: Boundary, view: SimulationView): Behavior[API] =
    var bodiesJ: util.ArrayList[Body] = toJava(bodies)

    Behaviors.receive{
      case (ctx, API.UpdateGUI(vt, it, bodies_s, bounds_s, from)) =>
        bodiesJ = toJava(bodies_s)
        view.setBodies(bodiesJ)
        view.setBounds(bounds_s)
        view.display(vt, it)
        from ! API.Msg(START, Array(), null)
        Behaviors.same
    }
