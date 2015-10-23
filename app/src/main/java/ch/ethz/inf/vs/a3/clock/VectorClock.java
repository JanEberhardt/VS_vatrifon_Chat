package ch.ethz.inf.vs.a3.clock;

/**
 * Created by jan on 23.10.15.
 */
public class VectorClock implements Clock {
    @Override
    public void update(Clock other) {

    }

    @Override
    public void setClock(Clock other) {

    }

    @Override
    public void tick(Integer pid) {

    }

    @Override
    public boolean happenedBefore(Clock other) {
        return false;
    }

    @Override
    public void setClockFromString(String clock) {

    }

    public int getTime(int pid){
        //todo
        return 0;
    }

    public void addProcess(int pid, int ime){
        //todo
    }

}
