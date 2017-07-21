package fi.peltoset.mikko.cameraslider;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class CameraSliderApplication extends Application {

  public SharedPreferences preferences;
  public SharedPreferences.Editor preferencesEditor;

  public static final String PREFERENCES_EXTRA_DEVICE_NAME = "PREFERENCES_EXTRA_DEVICE_NAME";
  public static final String PREFERENCES_EXTRA_DEVICE_ADDRESS = "PREFERENCES_EXTRA_DEVICE_ADDRESS";

  @Override
  public void onCreate() {
    super.onCreate();

    preferences = getApplicationContext().getSharedPreferences("fi.peltoset.mikko.cameraslider.preferences", Context.MODE_PRIVATE);
    preferencesEditor = preferences.edit();
  }
}
