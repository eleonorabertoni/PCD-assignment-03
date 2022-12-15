package ass01;

import Utils.Physics;
import ass01.basic.AbstractSimulator;
import ass01.basic.Body;
import ass01.basic.SimulationView;
import ass01.basic.V2d;

public class SerialSimulator extends AbstractSimulator {

	public SerialSimulator(SimulationView viewer, int nBodies, int dimSimulation) {
		super(viewer, nBodies, dimSimulation);
		viewer.setBounds(bounds);
		viewer.setBodies(bodies);
	}
	
	@Override
	public void execute(long nSteps) {

		/* init virtual time */

		vt = 0;
		dt = 0.001;

		long iter = 0;

		/* simulation loop */

		while (iter < nSteps) {

			/* update bodies velocity */

			for (int i = 0; i < bodies.size(); i++) {
				Body b = bodies.get(i);

				/* compute total force on bodies */
				V2d totalForce = Physics.computeTotalForceOnBody(b, bodies);

				/* compute instant acceleration */
				V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

				/* update velocity */
				b.updateVelocity(acc, dt);
			}

			/* compute bodies new pos */

			for (Body b : bodies) {
				b.updatePos(dt);
			}

			/* check collisions with boundaries */

			for (Body b : bodies) {
				b.checkAndSolveBoundaryCollision(bounds);
			}

			/* update virtual time */

			vt = vt + dt;
			iter++;

			/* display current stage */

//			viewer.display( vt, iter);

		}
	}
}
