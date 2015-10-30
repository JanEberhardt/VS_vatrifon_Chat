package ch.ethz.inf.vs.a3.udpclient;

import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.ErrorCodes;
import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageTypes;

public class UDPWorker extends AsyncTask<Object, Void, Pair<Integer, List<String>>> {
    private static final String LOG_TAG = "###udpworker";

    private Message request;
    private ResponseInterface responseInterface;

    public UDPWorker(Message request, ResponseInterface responseInterface){
        super();
        this.responseInterface = responseInterface;
        this.request = request;
    }

    public UDPWorker(String username, UUID uuid, ResponseInterface responseInterface) {
        this(new Message(username, uuid, MessageTypes.RETRIEVE_CHAT_LOG), responseInterface);
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
            Log.d(LOG_TAG, "request: " + request.getJson().toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            s.close();
            return new Pair<>(ErrorCodes.UDP_ERROR, null);
        }


        // send the request and wait for a result, if timeout occurred do it again!
        for(int i = NetworkConsts.RETRY_COUNT;i>0;i--){

            if(i<5)
                Log.d(LOG_TAG, "timeout occurred, retrying");

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

            List<String> result = new ArrayList<>();
            byte[] response = new byte[NetworkConsts.PAYLOAD_SIZE];
            p = new DatagramPacket(response, response.length);


            while (true) {
                try {
                    s.setSoTimeout(NetworkConsts.SOCKET_TIMEOUT);
                    s.receive(p);
                    result.add(new String(response, 0, p.getLength()));
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

            if(result.size() != 0) {
                s.close();
                return new Pair<>(-1, result);
            }
        }
        s.close();
        return new Pair<>(ErrorCodes.TIMEOUT, null);
    }

    @Override
    protected void onPostExecute(Pair<Integer, List<String>> result) {
        int errorCode = result.first;
        List<String> data = result.second;
        if (errorCode != -1) {
            Log.d(LOG_TAG, "received error message. error code: "+errorCode);
            responseInterface.handleError(errorCode);
        } else {
            Log.d(LOG_TAG, "received response: "+data);
            responseInterface.handleResponse(data);
        }
    }
}
