package com.ivanvolosyuk.sharetobrowser;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SenderDetails extends Activity {
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
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    
    add(layout, "Share to Browser", 26);
    add(layout, "", 20);
    add(layout, "Sending urls to browser.", 15);
    add(layout, "Cancel sending and notification settings is not implemented yet.", 15);
    setContentView(layout);
  }
}
