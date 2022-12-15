package ass01;

public interface Barrier {

    void hitAndWaitAll() throws InterruptedException;

    void setFinalBarrier();
}
