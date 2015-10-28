package ch.ethz.inf.vs.a3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.ErrorCodes;
import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageComparator;
import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.ResponseInterface;
import ch.ethz.inf.vs.a3.udpclient.UDPWorker;

public class ChatActivity extends AppCompatActivity {
    private static final String LOG_TAG = "###chatactivity";

    private String username;
    private UUID uuid;
    private ListView lstChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        username = getIntent().getStringExtra("username");
        uuid = (UUID) getIntent().getSerializableExtra("uuid");

        TextView usernameView = (TextView) findViewById(R.id.txt_username);
        TextView uuidView = (TextView) findViewById(R.id.txt_uuid);
        Button btnUpdate = (Button) findViewById(R.id.btn_update);
        Button btnLogOut = (Button) findViewById(R.id.btn_log_out);
        lstChat = (ListView) findViewById(R.id.lst_chat_messages);

        String usernameString = "username: " + username;
        usernameView.setText(usernameString);
        String uuidString = "uuid: " + uuid.toString();
        uuidView.setText(uuidString);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UDPWorker worker = new UDPWorker(username, uuid, new ResponseInterface() {
                    @Override
                    public void handleResponse(List<String> data) {
                        ChatActivity.this.handleResponseChatLog(data);
                    }

                    @Override
                    public void handleError(int errorCode) {
                        ChatActivity.this.handleErrorChatLog(errorCode);
                    }
                });
                worker.execute();
            }
        });
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Message req = new Message(username, uuid, MessageTypes.DEREGISTER);
        UDPWorker worker = new UDPWorker(req, new ResponseInterface() {
            @Override
            public void handleResponse(List<String> data) {
                ChatActivity.this.handleResponseDeregister(data);
            }

            @Override
            public void handleError(int errorCode) {
                ChatActivity.this.handleErrorDeregister(errorCode);
            }
        });
        worker.execute();
    }

    /**
     * Handle server response on a retrieve-chat-log request.
     */
    public void handleResponseChatLog(List<String> data) {

        // convert data
        PriorityQueue<Message> messageBuffer = new PriorityQueue<>(data.size(), new MessageComparator());
        for (String str : data) {
            try {
                JSONObject json = new JSONObject(str);
                Message msg = new Message(json);
                messageBuffer.add(msg);
            } catch (JSONException e) {
                // handle parsing error in response data
                handleErrorChatLog(ErrorCodes.MSG_PARSING_FAILED);
            }
        }

        List<String> chatLog = new ArrayList<>(messageBuffer.size());
        while (!messageBuffer.isEmpty()) {
            Message msg = messageBuffer.remove();
            try {
                String chatUser = msg.getJson().getJSONObject("header").getString("username");
                String chatMessage = msg.getJson().getJSONObject("body").getString("content");
                chatLog.add(String.format("<%s> %s", chatUser, chatMessage));
            } catch (JSONException e) {
                handleErrorChatLog(ErrorCodes.MSG_PARSING_FAILED);
            }
        }
        lstChat.setAdapter(new ArrayAdapter<>(this, R.layout.chat_log_entry, chatLog));
    }

    /**
     * Handle server error on retrieve-chat-log request.
     */
    public void handleErrorChatLog(int errorCode) {
        String text = ErrorCodes.getStringError(errorCode);
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Handle server response on a deregister request.
     */
    public void handleResponseDeregister(List<String> data) {
        try {
            Message msg = new Message(new JSONObject(data.get(0)));
            if (msg.type.equals(MessageTypes.ACK_MESSAGE)) {
                Log.d(LOG_TAG, "deregistration successful");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle server error on a deregister request.
     */
    public void handleErrorDeregister(int errorCode) {
        String text = ErrorCodes.getStringError(errorCode);
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
