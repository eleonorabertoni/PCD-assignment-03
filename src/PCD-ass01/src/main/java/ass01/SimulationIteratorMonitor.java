package ass01;

public interface SimulationIteratorMonitor extends StopSimulation {
    void incrementCounter();

    boolean isSimulationNotOver();

    void stop();
}
