package ch.ethz.inf.vs.a3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import ch.ethz.inf.vs.a3.message.ErrorCodes;
import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.ResponseInterface;
import ch.ethz.inf.vs.a3.udpclient.UDPWorker;

public class MainActivity extends AppCompatActivity implements ResponseInterface {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set the on-chance listener for the username text field
        // todo: can this be a problem with asynchronous stuff if you're really fast?
        EditText usernameText = (EditText) findViewById(R.id.username);
        usernameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MainActivity.this.username = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        username = usernameText.getText().toString();

        Button register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message req = new Message(MainActivity.this.username, MessageTypes.REGISTER);
                UDPWorker worker = new UDPWorker(req, MainActivity.this);
                worker.execute();
                // after this request we will still have 4 requests left...
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void handleResponse(Message msg) {
        Log.d("###", "response: "+msg.getJson().toString());
        if(msg.type.equals(MessageTypes.ACK_MESSAGE)){
            Intent i = new Intent(getApplicationContext(), ChatActivity.class);
            i.putExtra("username", username);
            i.putExtra("uuid", msg.uuid);
            startActivity(i);
        }
        else if(msg.type.equals(MessageTypes.ERROR_MESSAGE)){
            Log.d("###", "received message of type: "+msg.type);
            int errorCode;
            try {
                errorCode = msg.getJson().getJSONObject("body").getInt("content");
                String text = ErrorCodes.getStringError(errorCode);
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else{
            Log.d("###", "received message of type: "+msg.type);
        }
    }
}
