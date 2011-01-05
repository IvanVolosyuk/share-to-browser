package com.ivanvolosyuk.sharetobrowser;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class BackupHelper extends BackupAgentHelper {
  @Override
  public void onCreate() {
    super.onCreate();
    addHelper("id", new SharedPreferencesBackupHelper(this, "id"));
  }
}
