package com.ivanvolosyuk.sharetobrowser;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class Sender extends Service {
  private static final String TAG = "sendtocomputer";
  private final Handler handler = new Handler();
  
  static final String[] PROJECTION = new String[] {
      UrlStore.URL, UrlStore.BROWSER_ID
    };
  static final String SELECTION =
    UrlStore.URL + " = ? AND " + UrlStore.BROWSER_ID + " = ?";

  @Override
  public IBinder onBind(Intent intent) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public void onCreate() {
    super.onCreate();
  }
  
  private boolean pendingRequest = false;
  private boolean running = false;
  
  public class Entry {
    public String url;
    public String id;
  }

  class DBLookup extends AsyncTask<Void, Void, Entry> {
    @Override
    protected Entry doInBackground(Void... params) {
      Log.d(TAG, "db lookup background");
      Cursor c = getContentResolver().query(
          UrlStore.CONTENT_URI, PROJECTION, null, null, null);
      if (!c.moveToFirst()) {
        c.close();
        Log.d(TAG, "db lookup no url");
        return null;
      }
      
      Entry e = new Entry();
      e.url = c.getString(0);
      e.id = c.getString(1);
      Log.d(TAG, "db lookup url: " + e.url);
      c.close();
      return e;
    }
    
    @Override
    protected void onPostExecute(Entry result) {
      if (result == null) {
        stopLookup();
        return;
      } else {
        new NetSend().execute(result);
      }
    }
  }
  
  class NetSend extends AsyncTask<Entry, Void, Boolean> {
    @Override
    protected Boolean doInBackground(Entry... params) {
      Entry e = params[0];
      Log.d(TAG, "net send url: " + e.url);
      boolean success = sendRequest(e.url, e.id);
      if (!success) {
        return false;
      }
      String[] selectionArgs = new String[] { e.url, e.id };
      getContentResolver().delete(UrlStore.CONTENT_URI, SELECTION, selectionArgs);
      return true;
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
      if (result) {
        startLookup();
      } else {
        running = false;
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            if (running == false) {
              startLookup();
            }
          }
        }, 300000);
        
      }
    }
  }
  
  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    
    if (intent.getBooleanExtra("stop", false)) {
      hideNotification();
      cancelUpload();
      return;
    }
    
    showNotification();
    pendingRequest = true;
    if (!running) {
      startLookup();
    }
  }
  
  private void startLookup() {
    running = true;
    pendingRequest = false;
    new DBLookup().execute();
  }
  
  private void cancelUpload() {
    getContentResolver().delete(UrlStore.CONTENT_URI, null, null);
  }
  
  private void stopLookup() {
    running = false;
    if (pendingRequest) {
      startLookup();
    } else {
      hideNotification();
      stopSelf();
    }
  }
  
  private void showNotification() {
    String tickerText = "Got new URL";
    long when = System.currentTimeMillis();
    Notification notification = new Notification(R.drawable.icon, tickerText, when);
    
    Cursor c = getContentResolver().query(
        UrlStore.CONTENT_URI, PROJECTION, null, null, null);
    int numUrls = c.getCount();

    Context context = getApplicationContext();
    CharSequence contentTitle = (numUrls == 1 ? "One URL" : numUrls + " URLs") + " to be sent";
    CharSequence contentText = null;
    Intent notificationIntent = new Intent(this, SenderDetails.class);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    NotificationManager mgr =
      (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mgr.notify(0, notification);
  }
  
  private void hideNotification() {
    NotificationManager mgr =
      (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mgr.cancelAll();
  }
  
  /**
   * Slow network operation to send url to server.
   * @param url
   * @return
   */
  private boolean sendRequest(String url, String id) {
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
      Log.e(TAG, "send", e);
      return false;
    }
    Log.d(TAG, "sending request finished");
    return true;
  }
}
