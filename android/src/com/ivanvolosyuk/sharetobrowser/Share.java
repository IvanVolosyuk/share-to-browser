package com.ivanvolosyuk.sharetobrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Share extends Activity {
  private String id;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Associate.ensureBackup(this);
    setContentView(R.layout.message);
    TextView msg = (TextView) findViewById(R.id.message);
    ImageView icon = (ImageView) findViewById(R.id.icon);
    
    id = getSharedPreferences("id", Activity.MODE_PRIVATE).getString("id", null);
    Log.d("send-to-computer", "share for id: " + id);

    
    final String url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
    
    if (url == null) {
      msg.setText("No URL");
      return;
    }
    
    String prefix="http://send-to-computer.appspot.com/associate?browser=";
    if (url.startsWith(prefix)) {
      Associate.associate(this, url.substring(prefix.length(), url.length()));
      msg.setText("Associated with browser");
      return;
    }

    if (id == null) {
      msg.setText("No browser assigned");
      Intent i = new Intent(this, HelpPage.class);
      startActivity(i);
      finish();
      return;
    }
    
    final Handler handler = new Handler();
    
    new AsyncTask<Void,Void,Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        
        Cursor c = getContentResolver().query(
            UrlStore.CONTENT_URI, Sender.PROJECTION,
            Sender.SELECTION, new String[] { url, id }, null);
        boolean exist = c.moveToFirst();
        c.close();
          
        if (!exist) {
          ContentValues values = new ContentValues(2);
          values.put(UrlStore.URL, url);
          values.put(UrlStore.BROWSER_ID, id);
          getContentResolver().insert(UrlStore.CONTENT_URI, values);
        }

        startService(new Intent(Share.this, Sender.class));
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            finish();
          }
        }, 1000);
        return null;
      }
    }.execute();
    msg.setText("URL received");
    icon.setImageResource(android.R.drawable.ic_dialog_info);
  }
}
