package ass01;

public class SimpleWaitMonitorImpl implements SimpleWaitMonitor {

    @Override
    public synchronized void simpleWait(){
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void simpleNotify(){
        notify();
    }
}
