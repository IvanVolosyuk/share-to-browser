package com.ivanvolosyuk.sharetobrowser;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpPage extends Activity {
  private String getText(HttpResponse response) throws IOException {
    byte[] data = new byte[10];
    int len = response.getEntity().getContent().read(data);
    return new String(data, 0, len, "UTF-8");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Associate.ensureBackup(this);
    setContentView(R.layout.main);
    EditText editText = (EditText) findViewById(R.id.url);
    editText.setFilters(new InputFilter[] {
        new InputFilter() {
          @Override
          public CharSequence filter(CharSequence source, int start, int end,
              Spanned dest, int dstart, int dend) {
            return source.length() < 1 ? dest.subSequence(dstart, dend) : "";
          }
        }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    final LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    SharedPreferences prefs = getSharedPreferences("id", Activity.MODE_PRIVATE);
    final String id = prefs.getString("id", null);
    boolean hasId = id != null; 

    findViewById(R.id.has_browser_id).setVisibility(hasId ? View.VISIBLE : View.GONE);
    findViewById(R.id.no_browser_id).setVisibility(hasId ? View.GONE : View.VISIBLE);

    if (!hasId) return;
    
    final String url = "http://send-to-computer.appspot.com/me?browser=" + id;
    EditText editText = (EditText) findViewById(R.id.url);
    InputFilter[] filters = editText.getFilters();
    editText.setFilters(new InputFilter[] {});
    editText.setText(url);
    editText.setFilters(filters);
    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    
    final TextView status = (TextView) findViewById(R.id.count);
    status.setVisibility(View.GONE);
    final Handler handler = new Handler();
    
    (new Thread() {
      public void run() {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://send-to-computer.appspot.com/count?browser=" + id);
        try {
          HttpResponse response;
          response=httpclient.execute(httpGet);
          if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("wrong response code");
          }
          String text = getText(response);
          int count = Integer.parseInt(text);
          final String message;
          if (count == 0) {
            message = "You have no unread pages";
          } else {
            if (count == 1) {
              message = String.format("You have 1 unread page");
            } else {
              message = String.format("You have %d unread pages", count);
            }
          }
          handler.post(new Runnable() {
            @Override
            public void run() {
              status.setVisibility(View.VISIBLE);
              status.setText(message);
            }
          });
        } catch (Throwable e) {
          Log.e("send-to-computer", "send", e);
        }
      }
    }).start();
  }
}
