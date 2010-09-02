package com.ivanvolosyuk.sharetobrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Share extends Activity {
  private String id;
  
  private void add(LinearLayout layout, String message, int size) {
    TextView v = new TextView(this);
    v.setText(message);
    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    layout.addView(v);
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    id = getSharedPreferences("id", Activity.MODE_PRIVATE).getString("id", null);
    Log.d("send-to-computer", "share for id: " + id);

    
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    String url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
    
    String prefix="http://send-to-computer.appspot.com/associate?browser=";
    if (url.startsWith(prefix)) {
      Associate.associate(this, layout, url.substring(prefix.length(), url.length()));
      setContentView(layout);
      return;
    }

    if (id == null) {
      add(layout, "Send to Computer", 26);
      add(layout, "", 20);
      add(layout, "Please associate your device with a browser window before sharing.", 15);
      add(layout, "Open the address on your computer:", 15);
      add(layout, "http://send-to-computer.appspot.com/", 26);
      setContentView(layout);
      return;
    }
    
    Intent i = new Intent(this, Sender.class);
    i.putExtra("id", id);
    i.putExtra("url", url);
    startService(i);
    finish();
  }
}
