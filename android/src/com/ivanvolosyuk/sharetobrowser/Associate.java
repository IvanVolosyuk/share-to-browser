package com.ivanvolosyuk.sharetobrowser;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Associate extends Activity {
  private static void add(Activity activity, LinearLayout layout, String message, int size) {
    TextView v = new TextView(activity);
    v.setText(message);
    v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    layout.addView(v);
  }
  
  static class Backup {
    void requestBackup(Context context) {}
    
    static Backup newInstance() {
      final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
      if (sdkVersion < Build.VERSION_CODES.FROYO) {
        return new Backup();
      }
      return new RealBackup();
    }
  };

  static class RealBackup extends Associate.Backup {
    RealBackup() {}
    void requestBackup(Context context) {
      BackupManager mgr = new BackupManager(context);
      mgr.dataChanged();
    }
  };
  
  static Backup backup = Backup.newInstance();
  
  static void ensureBackup(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("id", Activity.MODE_PRIVATE);
    if (prefs.contains("id") && prefs.getBoolean("backup", false) == false) {
      // need to backup
      // pass through
    } else {
      // everything ok
      return;
    }
    Editor e = prefs.edit();
    e.putBoolean("backup", true);
    e.commit();
    backup.requestBackup(context);
  }
  
  public static boolean associate(Activity activity, String id) {
    boolean success = false;
    if (id != null) {
      Editor editor = activity.getSharedPreferences("id", Activity.MODE_PRIVATE).edit();
      editor.putString("id", id);
      editor.putBoolean("backup", true);
      editor.commit();
      backup.requestBackup(activity);
      success = true;
    }
    return success;
  }
  
  public static void associate(Activity activity, LinearLayout layout, String id) {
    boolean success = associate(activity, id);
    add(activity, layout, "Send to Computer", 26);
    add(activity, layout, "", 16);
    if (success) {
      add(activity, layout, "Congratulation! You phone is associated with the browser window. Now you can 'Share' pages and they will appear as new windows or tabs in your browser. Don't forget to disable popup blocker.", 18);
    } else {
      add(activity, layout, "Oops, there was a problem associating with your browser window. Please contact developer if the problem persist.", 18);
    }
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);

    String id = null;
    Intent i = getIntent();
    
    if (i != null) {
      if (i.getData() != null) {
        id = i.getData().getQueryParameter("browser");
      }
    }
    
    associate(this, layout, id);
    setContentView(layout);
  }
}
