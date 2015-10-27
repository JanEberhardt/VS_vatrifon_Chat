package ch.ethz.inf.vs.a3.message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by jan on 23.10.15.
 */
public class Message {

    private JSONObject json;
    public String type;
    public UUID uuid;

    public Message(JSONObject json) throws JSONException {
        this.json=json;
        this.type = json.getJSONObject("header").getString("type");
        this.uuid = UUID.fromString(json.getJSONObject("header").getString("uuid"));
    }

    public Message(String username, String type){
        this(username, UUID.randomUUID(), type);
    }

    public Message(String username, UUID uuid, String type){
        this(username, uuid, type, "{}");
    }

    public Message(String username, UUID uuid, String type, String timestamp) {
        this.type = type;
        this.uuid = uuid;
        this.json = new JSONObject();
        JSONObject header = new JSONObject();
        try {
            header.put("username", username);
            header.put("uuid", uuid.toString());
            header.put("timestamp", timestamp);
            header.put("type", type);
            this.json.put("header", header);
            this.json.put("body", new JSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson(){
        return json;
    }
}
