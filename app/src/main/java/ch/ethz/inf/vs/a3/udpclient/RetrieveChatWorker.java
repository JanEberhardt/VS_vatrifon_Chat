package ch.ethz.inf.vs.a3.udpclient;

import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.ErrorCodes;
import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageComparator;
import ch.ethz.inf.vs.a3.message.MessageTypes;

/**
 * Created by posedge on 26.10.15.
 */
public class RetrieveChatWorker extends AsyncTask<Object, Void, Pair<Integer, List<String>>> {

    private Message request;
    private ChatLogResponseInterface responseInterface;

    public RetrieveChatWorker(String username, UUID uuid, ChatLogResponseInterface responseInterface) {
        super();
        Message req = new Message(username, uuid, MessageTypes.RETRIEVE_CHAT_LOG);
        this.responseInterface = responseInterface;
        this.request = req;
    }

    @Override
    protected Pair<Integer, List<String>> doInBackground(Object... params) {
        // initialize the socket
        DatagramSocket s;
        try {
            s = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            return new Pair<>(ErrorCodes.UDP_ERROR, null);
        }

        // make a dns lookup
        InetAddress server_address;
        try {
            server_address = InetAddress.getByName(NetworkConsts.SERVER_ADDRESS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            s.close();
            return new Pair<>(ErrorCodes.UNKNOWN_HOST, null);
        }

        // create the request byte-array
        int msg_length = request.getJson().toString().length();
        byte[] message;
        try {
            message = request.getJson().toString().getBytes("utf-8");
            Log.d("###", "request: " + request.getJson().toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            s.close();
            return new Pair<>(ErrorCodes.UDP_ERROR, null);
        }

        // put the byte-array in a udp packet
        DatagramPacket p = new DatagramPacket(
                message,
                msg_length,
                server_address,
                NetworkConsts.UDP_PORT);

        // try to send the packet
        try {
            s.send(p);
        } catch (IOException e) {
            e.printStackTrace();
            s.close();
            return new Pair<>(ErrorCodes.UDP_ERROR, null);
        }

        List<String> chatLogRaw = new ArrayList<>();
        byte[] response = new byte[NetworkConsts.PAYLOAD_SIZE];
        p = new DatagramPacket(response, response.length);

        while (true) {
            try {
                s.setSoTimeout(NetworkConsts.SOCKET_TIMEOUT);
                s.receive(p);
                chatLogRaw.add(new String(response, 0, p.getLength()));
            } catch (SocketTimeoutException e) {
                break;
            } catch (SocketException e) {
                e.printStackTrace();
                s.close();
                return new Pair<>(ErrorCodes.UDP_ERROR, null);
            } catch (IOException e) {
                e.printStackTrace();
                s.close();
                return new Pair<>(ErrorCodes.UDP_ERROR, null);
            }
        }
        s.close();

        // convert data
        PriorityQueue<Message> messageBuffer = new PriorityQueue<>(chatLogRaw.size(), new MessageComparator());
        for(String str: chatLogRaw){
            try {
                JSONObject json = new JSONObject(str);
                Message msg = new Message(json);
                messageBuffer.add(msg);
            } catch (JSONException e) {
                return new Pair<>(ErrorCodes.MSG_PARSING_FAILED, null);
            }
        }

        List<String> chatLog = new LinkedList<>();
        while(!messageBuffer.isEmpty()){
            Message msg = messageBuffer.remove();
            String chatUser = null;
            try {
                chatUser = msg.getJson().getJSONObject("header").getString("username");
                String chatMessage = msg.getJson().getJSONObject("body").getString("content");
                chatLog.add(String.format("<%s> %s", chatUser, chatMessage));
            } catch (JSONException e) {
                return new Pair<>(ErrorCodes.MSG_PARSING_FAILED, null);
            }
        }
        return new Pair<>(-1, chatLog);
    }

    @Override
    protected void onPostExecute(Pair<Integer, List<String>> result) {
        JSONObject temp;
        Message res = null;
        int errorCode = result.first;

        try {
            // return the appropriate error message if something went wrong...
            if (errorCode != -1) {
                res = new Message("UDPWorker", MessageTypes.ERROR_MESSAGE);
                res.getJson().getJSONObject("body").put("content", errorCode);
                responseInterface.handleError(res);
            } else {
                responseInterface.handleResponse(result.second);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
