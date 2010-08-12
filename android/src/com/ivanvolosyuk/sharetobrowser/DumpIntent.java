package com.ivanvolosyuk.sharetobrowser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DumpIntent extends Activity {
  private void add(LinearLayout layout, String message) {
    TextView v = new TextView(this);
    v.setText(message);
    layout.addView(v);
  }
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    Intent i = getIntent();
    if (i == null) {
      add(layout, "No intent");
    } else {
      
      if (i.getData() != null) {
        add(layout, "URI: "  + i.getData().toString());
        String id = i.getData().getQueryParameter("browser");
        add(layout, "ID: " + id);
      }
      
      
      Bundle b = i.getExtras();
      if (b == null) {
        add(layout, "No bundle");
      } else {
        for (String key : b.keySet()) {
          Object o = b.get(key);
          if (o == null) {
            add(layout, key + ": null"); 
          } else {
            add(layout, key + ":" + o.getClass().getName() + ":" + o);
          }
        }
      }
    }

    setContentView(layout);
  }
}