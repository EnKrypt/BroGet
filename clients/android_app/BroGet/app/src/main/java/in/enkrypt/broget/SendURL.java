package in.enkrypt.broget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class SendURL extends Activity {

    Uri url=null;
    EditText pidlist=null;
    EditText uidlist=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_url);

        url=getIntent().getData();

        pidlist=(EditText)findViewById(R.id.pidview);
        uidlist=(EditText)findViewById(R.id.uidview);
        uidlist.setText("[\""+Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)+"\"]");
        Button button=(Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sender = new Intent(v.getContext(), Sender.class);
                sender.putExtra("url", url);
                sender.putExtra("pidlist",pidlist.getText().toString());
                sender.putExtra("uidlist",uidlist.getText().toString());
                startService(sender);
                Toast.makeText(getApplicationContext(), "Bro, request sent.", Toast.LENGTH_LONG).show();

                finish();
            }
        });
    }
}
