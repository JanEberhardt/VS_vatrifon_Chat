package ch.ethz.inf.vs.a3.clock;

/**
 * Created by jan on 23.10.15.
 * <p/>
 * simple implementation of LamportClock
 */
public class LamportClock implements Clock {

    private int time;

    @Override
    public void update(Clock other) {
        LamportClock otherL = (LamportClock) other;
        if (otherL.getTime() > getTime())
            time = otherL.getTime();
    }

    @Override
    public void setClock(Clock other) {
        LamportClock otherL = (LamportClock) other;
        time = otherL.getTime();
    }

    @Override
    public void tick(Integer pid) {
        time++;
    }

    @Override
    public boolean happenedBefore(Clock other) {
        return getTime() < ((LamportClock) other).getTime();
    }

    @Override
    public String toString() {
        return time + "";
    }

    @Override
    public void setClockFromString(String clock) {
        try {
            time = Integer.parseInt(clock);
        } catch (NumberFormatException e) {
        }
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }
}
