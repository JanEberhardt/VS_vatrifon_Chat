package ch.ethz.inf.vs.a3.udpclient;

import android.os.AsyncTask;
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

import ch.ethz.inf.vs.a3.message.ErrorCodes;
import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageTypes;

/**
 * Created by jan on 26.10.15.
 *
 * executes a request using udp, retries a number of times according to NetworkConsts
 */
public class UDPWorker extends AsyncTask<Object, Void, String> {

    private ResponseInterface responseInterface;
    private Message request;
    private int requestsLeft;

    public UDPWorker(Message request, ResponseInterface responseInterface) {
        super();
        this.responseInterface = responseInterface;
        this.request = request;
        this.requestsLeft = NetworkConsts.RETRY_COUNT;
    }

    @Override
    protected String doInBackground(Object... params) {
        // initialize the socket
        DatagramSocket s;
        try {
            s = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            return ErrorCodes.UDP_ERROR + "";
        }

        // make a dns lookup
        InetAddress server_address;
        try {
            server_address = InetAddress.getByName(NetworkConsts.SERVER_ADDRESS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return ErrorCodes.UNKNOWN_HOST + "";
        }

        // create the request byte-array
        int msg_length = request.getJson().toString().length();
        byte[] message;
        try {
            message = request.getJson().toString().getBytes("utf-8");
            Log.d("###", "request: " + request.getJson().toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ErrorCodes.UDP_ERROR + "";
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
            return ErrorCodes.UDP_ERROR + "";
        }

        String text = ErrorCodes.TIMEOUT + "";
        byte[] response = new byte[NetworkConsts.PAYLOAD_SIZE];
        p = new DatagramPacket(response, response.length);

        for (int i = 0; i < requestsLeft; i++) {
            try {
                s.setSoTimeout(NetworkConsts.SOCKET_TIMEOUT);
                s.receive(p);
                text = new String(response, 0, p.getLength());
                break;
            } catch (SocketTimeoutException e) {
                Log.d("###", "UDPWorker: timeout occurred");
            } catch (SocketException e) {
                e.printStackTrace();
                return ErrorCodes.UDP_ERROR + "";
            } catch (IOException e) {
                e.printStackTrace();
                return ErrorCodes.UDP_ERROR + "";
            }
        }
        s.close();
        return text;
    }

    @Override
    protected void onPostExecute(String result) {
        JSONObject temp;
        Message res;
        int errorCode;
        try {
            errorCode = Integer.parseInt(result);
        } catch (NumberFormatException e) {
            errorCode = -1;
        }

        try {
            // return the appropriate error message if something went wrong...
            if (errorCode != -1) {
                res = new Message("UDPWorker", MessageTypes.ERROR_MESSAGE);
                res.getJson().getJSONObject("body").put("content", errorCode);
            }
            // otherwise return the response from the server
            else {
                temp = new JSONObject(result);
                res = new Message(temp);
            }
            responseInterface.handleResponse(res);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
