package ch.ethz.inf.vs.a3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
//todo: part 3!
public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        TextView username = (TextView) findViewById(R.id.username_view);
        username.setText("username: "+getIntent().getStringExtra("username"));
        TextView uuid = (TextView) findViewById(R.id.uuid_view);
        uuid.setText("uuid: "+getIntent().getSerializableExtra("uuid").toString());
    }
}
