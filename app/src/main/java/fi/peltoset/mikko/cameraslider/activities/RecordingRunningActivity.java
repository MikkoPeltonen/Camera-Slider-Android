package fi.peltoset.mikko.cameraslider.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import fi.peltoset.mikko.cameraslider.R;

public class RecordingRunningActivity extends AppCompatActivity {

  private LinearLayout fullscreenLayout;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_recording_running);

    fullscreenLayout = (LinearLayout) findViewById(R.id.fullscreenLayout);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);

    hideSystemUI();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
  }

  private void hideSystemUI() {
    fullscreenLayout.setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE
    );
  }
}
