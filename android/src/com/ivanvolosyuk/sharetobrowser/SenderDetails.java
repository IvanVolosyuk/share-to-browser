package com.ivanvolosyuk.sharetobrowser;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SenderDetails extends Activity {
  private void add(LinearLayout layout, String message, int size) {
    TextView v = new TextView(this);
    v.setText(message);
    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    v.setLines(1);
    layout.addView(v);
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sender_details);
    final LinearLayout layout = (LinearLayout) findViewById(R.id.list);
    new AsyncTask<Void, Void, List<String>>() {
      protected List<String> doInBackground(Void... params) {
        ArrayList<String> urls = new ArrayList<String>();
        Cursor c = getContentResolver().query(
            UrlStore.CONTENT_URI, Sender.PROJECTION, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
          String url = c.getString(0);
          urls.add(url);
          c.moveToNext();
          Log.d("sendtocomputer", "url = " + url);
        }
        c.close();
        return urls;
      };
      @Override
      protected void onPostExecute(List<String> urls) {
        for (String url : urls) {
          add(layout, url, 12);
        }
      }
    }.execute();
    Button cancel = (Button) findViewById(R.id.cancel);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(SenderDetails.this, Sender.class);
        i.putExtra("stop", true);
        startService(i);
        finish();
      }
    });
    Button ok = (Button) findViewById(R.id.ok);
    ok.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
  }
}
