package ass01;

import ass01.basic.AbstractSimulator;
import ass01.basic.SimulationView;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentSimulator extends AbstractSimulator {
    private final int nThreads;
    Barrier b1;
    Barrier b2;
    SimulationIteratorMonitor simulationIterator;

    public ConcurrentSimulator(SimulationView viewer, int nBodies, int dimSimulation, int nThreads) {
        super(viewer, nBodies, dimSimulation);
        this.nThreads = nThreads;
        if (viewer != null) {
            viewer.setBodies(bodies);
            viewer.setBounds(bounds);
        }
    }

    @Override
    public void execute(long nSteps) {
        simulationIterator = new SimulationIteratorMonitorImpl(nSteps, nThreads, viewer);
        b1 = new BarrierImpl(nThreads, simulationIterator);
        b2 = new BarrierImpl(nThreads, simulationIterator);

        if(viewer != null){ // set stop button handler
            SimulationView.VisualiserFrame.setStopHandler((a) -> {
                b1.setFinalBarrier();
                b2.setFinalBarrier();
            });
        }

        int myStart;
        int myEnd;
        List<Thread> threadList = new ArrayList<>();
        for(int i = 0; i < nThreads; i++){
            myStart = bodies.size() * i / nThreads;
            myEnd = bodies.size() * (i+1)/nThreads;
            threadList.add(startExecutor(myStart, myEnd));// run workers
        }

        //wait for conclusion of simulation
        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private Thread startExecutor(int start, int end){
        ExecutorThread ex = new ExecutorThread(bodies, start, end, bounds, b1, b2, simulationIterator);
        Thread t = new Thread(ex);
        t.start();
        return  t;
    }
}
