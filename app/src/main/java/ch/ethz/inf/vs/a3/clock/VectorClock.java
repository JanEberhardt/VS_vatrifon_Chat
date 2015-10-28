package ch.ethz.inf.vs.a3.clock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by jan on 23.10.15.
 *
 * implementation of VectorClocks
 */
public class VectorClock implements Clock {

    /**
     * keeps a mapping from pid to logical time
     */
    private Map<Integer, Integer> vector;

    public VectorClock(){
        this.vector = new HashMap<>();
    }

    @Override
    public void update(Clock other) {

        Map<Integer, Integer> temp = getMap(other);

        Set<Integer> keys = vector.keySet();
        for(int key:keys){
            if(temp.get(key) != null) {
                if(temp.get(key) > vector.get(key))
                    vector.put(key, temp.get(key));
            }
        }

        // needs to be done, because we also want to add processes that are not yet in our vector
        keys = temp.keySet();
        for(int key:keys){
            if(vector.get(key) == null) {
                vector.put(key, temp.get(key));
            }
        }
    }

    @Override
    public void setClock(Clock other) {
        setClockFromString(other.toString());
    }

    @Override
    public void tick(Integer pid) {
        int oldClock = getTime(pid);
        vector.put(pid, oldClock + 1);
    }

    @Override
    public boolean happenedBefore(Clock other) {

        // boolean accumulator, we AND all the necessary conditions to this
        boolean happenedBefore = true;

        Map<Integer, Integer> temp = getMap(other);
        Set<Integer> keys = vector.keySet();

        for(int key:keys){
            if(temp.get(key) != null) {
                happenedBefore = happenedBefore && vector.get(key) == temp.get(key);
            }
        }

        // in case all are equal
        if (happenedBefore)
            return false;

        // otherwise
        happenedBefore = true;
        for(int key:keys){
            if(temp.get(key) != null) {
                happenedBefore = happenedBefore && vector.get(key) <= temp.get(key);
            }
        }

        return happenedBefore;
    }

    @Override
    public void setClockFromString(String clock) {
        try {
            vector = getMapFromString(clock);
        } catch (NumberFormatException e){}
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Set<Integer> keys = vector.keySet();
        Iterator<Integer> it = keys.iterator();

        sb.append("{");

        // iterate over all the keys, if there are none, we just return {}
        while(it.hasNext()){
            int key = it.next();
            // represents a key value par, e.g: "1":2
            String keyVal = "\"" + key + "\"" + ":" + vector.get(key);
            sb.append(keyVal);
            if(it.hasNext())
                sb.append(",");
        }
        sb.append("}");

        return sb.toString();
    }

    public int getTime(int pid){
        return vector.get(pid);
    }

    public void addProcess(int pid, int time){
        vector.put(pid, time);
    }

    /**
     * Helper method that returns a pid -> logic-time map from a given vector-clock
     */
    private Map<Integer, Integer> getMap(Clock clock){
        return getMapFromString(clock.toString());
    }

    /**
     * Helper method that returns a pid -> logic-time map from a given string representation
     */
    private Map<Integer, Integer> getMapFromString(String clock) throws NumberFormatException{

        Map<Integer, Integer> res = new HashMap<>();

        String[] keyValues = clock.replaceAll("\\{", "").replaceAll("\\}", "").split(",");

        // if the string is empty, we want to return here, otherwise we get a
        // parse error later...
        if(keyValues[0].isEmpty())
            return res;

        for (String keyVal : keyValues) {
            String[] keyValSplit = keyVal.split(":");
            // because there are still "" around the key, we need to get rid of that!
            keyValSplit[0] = keyValSplit[0].replaceAll("\"", "");
            int key = Integer.parseInt(keyValSplit[0]);
            int val = Integer.parseInt(keyValSplit[1]);
            res.put(key, val);
        }

        return res;
    }
}
