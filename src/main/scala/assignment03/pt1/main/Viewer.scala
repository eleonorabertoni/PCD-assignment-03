package assignment03.pt1.main

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment03.pt1.GUI.SimulationView
import assignment03.pt1.main.API.API
import assignment03.pt1.main.Body.Body

import java.util

object Viewer:

  private def toJava(bodies: Seq[Body]): util.ArrayList[Body] =
    val temp = util.ArrayList[Body]()
    for b <- bodies do temp.add(b)
    temp

  def apply(bodies: Seq[Body], bounds: Boundary, view: SimulationView): Behavior[API] =
    var bodiesJ: util.ArrayList[Body] = toJava(bodies)

    Behaviors.receive{
      case (ctx, API.UpdateGUI(vt, it, bodies_s, bounds_s, from)) =>
        bodiesJ = toJava(bodies_s)
        view.setBodies(bodiesJ)
        view.setBounds(bounds_s)
        view.display(vt, it)
        from ! API.Msg("Inizio", Seq(), null)
        Behaviors.same
    }
