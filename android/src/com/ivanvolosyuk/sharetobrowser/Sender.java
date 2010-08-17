package com.ivanvolosyuk.sharetobrowser;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class Sender extends Service {

  @Override
  public IBinder onBind(Intent intent) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public void onCreate() {
    super.onCreate();
  }
  
  private synchronized void sendUrl(String url) {
    if (networkThread == null) {
      networkThread = new NetworkThread(url);
    } else {
      networkThread.addUrl(url);
    }
  }
  
  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    String url = intent.getStringExtra("url");
    
    String tickerText = "Sending url to browser";
    long when = System.currentTimeMillis();
    Notification notification = new Notification(R.drawable.icon, tickerText, when);
    
    Context context = getApplicationContext();
    CharSequence contentTitle = "Sending url to browser";
    CharSequence contentText = null;
    Intent notificationIntent = new Intent(this, SenderDetails.class);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    NotificationManager mgr =
      (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mgr.notify(0, notification);
    
    sendUrl(url);
  }
  
  private void uploadFinished() {
    synchronized (this) {
      if (networkThread != null) {
        return;
      }
    }
    
    NotificationManager mgr =
      (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mgr.cancelAll();    
    this.stopSelf();
  }
  
  class NetworkThread extends Thread {
    public NetworkThread(String url) {
      addUrl(url);
      start();
    }
    
    private boolean sendRequest(String url) {
      String id = getSharedPreferences("id", Activity.MODE_PRIVATE).getString("id", null);
      HttpClient httpclient = new DefaultHttpClient();
      HttpPost httpPost = new HttpPost("http://send-to-computer.appspot.com/submit");
      try {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        //Your DATA
        nameValuePairs.add(new BasicNameValuePair("browser", id));
        nameValuePairs.add(new BasicNameValuePair("url", url));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response;
        response=httpclient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != 200) {
          throw new IOException("wrong response code");
        }
        // success
      } catch (Throwable e) {
        Log.e("send-to-computer", "send", e);
        return false;
      }
      Log.d("send-to-computer", "sending request finished");
      return true;
    }
    
    public void run() {
      try {
        while (true) {
          String url = waitForNewUrl();
          if (url == null) break;
          while (!sendRequest(url)) {
            Thread.sleep(300000);
          }
        }
      } catch (InterruptedException e) {
      }
      uploadFinished();
    }
    
    private String waitForNewUrl() throws InterruptedException{
      synchronized (Sender.this) {
        if (urls.size() != 0) {
          return urls.remove(0);
        }
        Sender.this.networkThread = null;
        return null;
      }
    }
    
    public void addUrl(String url) {
      urls.add(url);
    }
    
    private void uploadFinished() {
      handler.post(new Runnable() {
        @Override
        public void run() {
          Sender.this.uploadFinished();
        }
      });
    }
    
    ArrayList<String> urls = new ArrayList<String>();
  }
  
  NetworkThread networkThread;
  Handler handler = new Handler();
}
