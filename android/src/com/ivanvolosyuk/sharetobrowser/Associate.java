package com.ivanvolosyuk.sharetobrowser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Associate extends Activity {
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

    boolean success = false;
    Intent i = getIntent();
    if (i != null) {
      if (i.getData() != null) {
        String id = i.getData().getQueryParameter("browser");
        if (id != null) {
          Editor editor = getSharedPreferences("id", Activity.MODE_PRIVATE).edit();
          editor.putString("id", id);
          editor.commit();
          success = true;
        }
      }
    }
    
    add(layout, "Send to Computer", 26);
    add(layout, "", 15);
    if (success) {
      add(layout, "Congratulation! You phone is associated with the browser window. Now you can 'Share' pages and they will appear as new windows or tabs in your browser. Don't forget to disable popup blocker.", 18);
    } else {
      add(layout, "Oops, there was a problem associating with your browser window. Please contact developer if the problem persist.", 18);
    }

    setContentView(layout);
  }
}
