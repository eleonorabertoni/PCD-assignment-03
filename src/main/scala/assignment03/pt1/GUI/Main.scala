package assignment03.pt1.GUI

import assignment03.pt1
import assignment03.pt1.Body.Body
import assignment03.pt1.{Boundary, P2d, V2d}
import assignment03.pt1.Utils.createBodies
import assignment03.pt1.GUI.SimulationView.VisualiserFrame.setStartHandler

import java.util

object Main extends App:
  val dim = 3
  val v = SimulationView(800, 800)
  var currentIteration: Int = 0
  val N_BODY: Int = 10
  val bounds: Boundary = Boundary(-dim, -dim, dim, dim)
  val bodies: Seq[Body] = createBodies(N_BODY, bounds)
  //val b: Body = pt1.Body(0, P2d(0,0), V2d(2,2), 50)
  val java: util.ArrayList[Body] = util.ArrayList()
  var vt: Double = 0
  for b <- bodies do
    java.add(b)
  v.setBodies(java)
  v.setBounds(bounds)
  /*
  setStartHandler((a) => {
    //monitor.simpleNotify();
    SimulationView.VisualiserFrame.setFocusOnSimulation();
  });*/
  //v.frame.
  v.display(vt, currentIteration)
  /*
  v.display(vt, currentIteration)
  v.display(vt, currentIteration)
  v.display(vt, currentIteration)
  v.display(vt, 22)*/

