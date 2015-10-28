package ch.ethz.inf.vs.a3.message;

import org.json.JSONException;

import java.util.Comparator;

import ch.ethz.inf.vs.a3.clock.VectorClock;

/**
 * Message comparator class. Use with PriorityQueue.
 */
public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(Message lhs, Message rhs) {
        try {
            VectorClock vcl, vcr;
            vcl = new VectorClock();
            vcl.setClockFromString(lhs.getJson().getJSONObject("header").getString("timestamp"));
            vcr = new VectorClock();
            vcr.setClockFromString(rhs.getJson().getJSONObject("header").getString("timestamp"));
            return vcl.happenedBefore(vcr) ? 1 : -1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
