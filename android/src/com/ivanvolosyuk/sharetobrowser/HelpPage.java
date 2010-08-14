package com.ivanvolosyuk.sharetobrowser;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpPage extends Activity {
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
    
    add(layout, "Send to Computer", 26);
    add(layout, "", 20);
    add(layout, "You can send web pages to your computer's browser using this program.", 15);
    add(layout, "On your computer's browser open:", 15);
    add(layout, "http://send-to-computer.appspot.com", 20);
    add(layout, "Follow instructions on the page.", 15);
    setContentView(layout);
  }
}
