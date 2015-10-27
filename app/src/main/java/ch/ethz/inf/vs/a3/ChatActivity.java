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

import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.ErrorCodes;
import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.ChatLogResponseInterface;
import ch.ethz.inf.vs.a3.udpclient.ResponseInterface;
import ch.ethz.inf.vs.a3.udpclient.RetrieveChatWorker;
import ch.ethz.inf.vs.a3.udpclient.UDPWorker;

//todo: part 3!
public class ChatActivity extends AppCompatActivity implements ResponseInterface, ChatLogResponseInterface {
    private static final String LOG_TAG = "###chatactivity";

    private String username;
    private UUID uuid;
    private ListView lstChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        username = getIntent().getStringExtra("username");
        uuid = (UUID)getIntent().getSerializableExtra("uuid");

        TextView usernameView = (TextView) findViewById(R.id.txt_username);
        TextView uuidView = (TextView) findViewById(R.id.txt_uuid);
        Button btnUpdate = (Button) findViewById(R.id.btn_update);
        Button btnLogOut = (Button) findViewById(R.id.btn_log_out);
        lstChat = (ListView) findViewById(R.id.lst_chat_messages);

        usernameView.setText("username: "+username);
        uuidView.setText("uuid: " + uuid.toString());
        btnUpdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                RetrieveChatWorker worker = new RetrieveChatWorker(username, uuid, ChatActivity.this);
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
        UDPWorker worker = new UDPWorker(req, this);
        worker.execute();
    }

    /**
     * Handle server response on a retrieve-chat-log request.
     */
    @Override
    public void handleResponse(List<String> data) {
        lstChat.setAdapter(new ArrayAdapter<String>(this, R.layout.chat_log_entry, data));
    }

    @Override
    public void handleError(Message msg) {
        Log.d(LOG_TAG, "retrieving chat log failed.");
        this.handleResponse(msg);
    }

    /**
     * Handle server response on a deregister request.
     */
    @Override
    public void handleResponse(Message msg) {
        Log.d(LOG_TAG, "response: "+msg.getJson().toString());
        switch (msg.type){
        case MessageTypes.ACK_MESSAGE:
            Log.d(LOG_TAG, "deregestration successful");
            break;
        case MessageTypes.ERROR_MESSAGE:
            Log.d("###", "received message of type: "+msg.type);
            int errorCode;
            try {
                errorCode = msg.getJson().getJSONObject("body").getInt("content");
                String text = ErrorCodes.getStringErrorServer(errorCode);
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            break;
        default:
            Log.d("###", "received message of type: "+msg.type);
        }
    }
}
