package ch.ethz.inf.vs.a3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.UUID;

import ch.ethz.inf.vs.a3.message.ErrorCodes;
import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.ResponseInterface;
import ch.ethz.inf.vs.a3.udpclient.UDPWorker;

//todo: part 3!
public class ChatActivity extends AppCompatActivity implements ResponseInterface {
    private static final String LOG_TAG = "###chatactivity";

    private String username;
    private UUID uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        username = getIntent().getStringExtra("username");
        uuid = (UUID)getIntent().getSerializableExtra("uuid");
        TextView usernameView = (TextView) findViewById(R.id.username_view);
        TextView uuidView = (TextView) findViewById(R.id.uuid_view);
        Button btnLogOut = (Button) findViewById(R.id.btn_log_out);
        usernameView.setText("username: "+username);
        uuidView.setText("uuid: " + uuid.toString());
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
