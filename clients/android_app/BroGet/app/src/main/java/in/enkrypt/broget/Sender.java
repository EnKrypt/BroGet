package in.enkrypt.broget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sender extends Service {
    private boolean isRunning  = false;
    public String APIURI = "192.168.0.102:8081/init";
    //Replace with appropriate url for your config
    public String TAG="BG";

    public Sender() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand");

        final String url=intent.getExtras().get("url").toString();
        final String pidlist=intent.getExtras().get("pidlist").toString();
        final String uidlist=intent.getExtras().get("uidlist").toString();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost post = new HttpPost("http://" + APIURI);
                    Map pair = new HashMap();
                    pair.put("link", URLEncoder.encode(url, "UTF-8"));
                    pair.put("pidlist", pidlist);
                    pair.put("uidlist", uidlist);
                    post.setEntity(new StringEntity(new JSONObject(pair).toString()));
                    post.setHeader("Accept","application/json");
                    post.setHeader("Content-type","application/json");
                    Log.d("response", URLEncoder.encode(url, "UTF-8")+" PIDLIST:"+pidlist+" UIDLIST:"+uidlist);
                    HttpResponse resp = client.execute(post);
                    HttpEntity ent = resp.getEntity();
                    String result = EntityUtils.toString(ent);
                    Log.d("response", result);

                    //CALLBACKS
                } catch (Exception e) {}
            }
        }).start();

        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {

        isRunning = false;

        Log.i(TAG, "Service onDestroy");
    }
}
