package in.enkrypt.broget;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BackgroundChecker extends Service{

    private boolean isRunning  = false;
    public String TAG="BG";

    public BackgroundChecker() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(true) {
                        Thread.sleep(10000);
                        Log.i(TAG, "Will check now to sync downloads");
                        final String APIURI="192.168.0.105:8081/download";
                        //Replace with appropriate url for your config
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    HttpClient client = new DefaultHttpClient();
                                    HttpPost post = new HttpPost("http://" + APIURI);
                                    List<NameValuePair> pair = new ArrayList<NameValuePair>(1);
                                    pair.add(new BasicNameValuePair("uid", URLEncoder.encode(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID), "UTF-8")));
                                    post.setEntity(new UrlEncodedFormEntity(pair));
                                    Log.d("response", "Trying to fetch list");
                                    HttpResponse resp = client.execute(post);
                                    HttpEntity ent = resp.getEntity();
                                    String result = EntityUtils.toString(ent);
                                    Log.d("response", result);

                                    JSONArray downloads=new JSONArray(result);
                                    for (int i=0;i<downloads.length();i++){
                                        String item= URLDecoder.decode(downloads.getString(i));
                                        Log.d("response","Fetching "+item);
                                        downloadfromurl(item);
                                    }

                                    //CALLBACKS
                                } catch (Exception e) {}
                            }
                        }).start();
                    }
                }
                catch (Exception e){}

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

    public void downloadfromurl(String urldata){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urldata));
        String[] spl=urldata.split("/");
        String filename=spl[spl.length-1];
        request.setDescription("Files are downloading");
        request.setTitle("Bro, "+filename+" is in sync.");
// in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

// get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}

