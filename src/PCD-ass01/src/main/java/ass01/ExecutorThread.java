package ass01;

import Utils.Physics;
import ass01.basic.Boundary;
import ass01.basic.V2d;
import ass01.basic.Body;

import java.util.List;

public class ExecutorThread implements Runnable {
    private final static double dt = 0.001;
    private final List<Body> bodies;
    private final int startInterval;
    private final int finishInterval;
    private final Boundary bounds;
    private final Barrier barrier1;
    private final Barrier barrier2;
    private final SimulationIteratorMonitor simulationIteratorMonitorImpl;

    public ExecutorThread(List<Body> bodies, int startInterval, int finishInterval, Boundary bounds, Barrier b1, Barrier b2, SimulationIteratorMonitor simulationMonitor) {
        this.bodies = bodies;
        this.startInterval = startInterval;
        this.finishInterval = finishInterval;
        this.bounds = bounds;
        this.barrier1 = b1;
        this.barrier2 = b2;
        this.simulationIteratorMonitorImpl = simulationMonitor;
    }

    public void run() {
        while (simulationIteratorMonitorImpl.isSimulationNotOver()) {
            simulationIteratorMonitorImpl.incrementCounter();
            calculateAndUpdateVelocity(startInterval, finishInterval);
            hitAndWait(barrier1);
            updatePositionAndCheckBoundaryCollision(startInterval, finishInterval);
            hitAndWait(barrier2);
        }
    }

    private void hitAndWait(Barrier b){
        try {
            b.hitAndWaitAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calculateAndUpdateVelocity(int localStart, int localEnd) {
        for (int i = localStart; i < localEnd; i++) {
            Body b = bodies.get(i);
            /* compute total force on bodies */
            V2d totalForce = Physics.computeTotalForceOnBody(b, bodies);

            /* compute instant acceleration */
            V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

            /* update velocity */
            b.updateVelocity(acc, dt);
        }
    }

    private void updatePositionAndCheckBoundaryCollision(int localStar, int localEnd) {
        for (int i = localStar; i < localEnd; i++) {
            Body b = bodies.get(i);
            b.updatePos(dt);
            b.checkAndSolveBoundaryCollision(bounds);
        }
    }
}
