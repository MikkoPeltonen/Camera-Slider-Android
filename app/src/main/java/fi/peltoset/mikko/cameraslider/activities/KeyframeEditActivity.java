package fi.peltoset.mikko.cameraslider.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import fi.peltoset.mikko.cameraslider.IncreaseDecreaseHandler;
import fi.peltoset.mikko.cameraslider.miscellaneous.KeyframePOJO;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.dialogs.ValuePickerDialog;
import fi.peltoset.mikko.cameraslider.interfaces.IncreaseDecreaseListener;
import fi.peltoset.mikko.cameraslider.interfaces.ValuePickerDialogInterface;


public class KeyframeEditActivity extends AppCompatActivity implements ValuePickerDialogInterface {

  private static final int TIME_PICKER = 1;

  private LinearLayout timePanel;
  private FloatingActionButton saveButton;

  private KeyframePOJO keyframe = new KeyframePOJO();
  private int keyframeIndex;

  private ImageButton slideLeft, slideRight;
  private ImageButton panCCW, panCW;
  private ImageButton tiltCCW, tiltCW;
  private ImageButton zoomCCW, zoomCW;
  private ImageButton focusCCW, focusCW;

  private TextView slideLengthTextView;
  private TextView panAngleTextView;
  private TextView tiltAngleTextView;
  private TextView zoomTextView;
  private TextView focusTextView;
  private TextView time;
  private TextView waitTime;

  private int fps;
  private int interval;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_keyframe_edit);

    Intent intent = getIntent();

    keyframeIndex = intent.getIntExtra("KEYFRAME_INDEX", -1);
    keyframe.setDuration(intent.getIntExtra("KEYFRAME_DURATION", 3000));
    keyframe.setSlideLength(intent.getIntExtra("KEYFRAME_SLIDE", 0));
    keyframe.setPanAngle(intent.getIntExtra("KEYFRAME_PAN", 0));
    keyframe.setTiltAngle(intent.getIntExtra("KEYFRAME_TILT", 0));
    keyframe.setZoom(intent.getIntExtra("KEYFRAME_ZOOM", 0));
    keyframe.setFocus(intent.getIntExtra("KEYFRAME_FOCUS", 0));

    fps = intent.getIntExtra("FPS", 1);
    interval = intent.getIntExtra("INTERVAL", 1);

    Toolbar toolbar = (Toolbar) findViewById(R.id.keyframeToolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    timePanel = (LinearLayout) findViewById(R.id.timePanel);
    saveButton = (FloatingActionButton) findViewById(R.id.saveButton);

    slideLeft = (ImageButton) findViewById(R.id.slideLeft);
    slideRight = (ImageButton) findViewById(R.id.slideRight);
    panCCW = (ImageButton) findViewById(R.id.panCCW);
    panCW = (ImageButton) findViewById(R.id.panCW);
    tiltCCW = (ImageButton) findViewById(R.id.tiltCCW);
    tiltCW = (ImageButton) findViewById(R.id.tiltCW);
    zoomCCW = (ImageButton) findViewById(R.id.zoomCCW);
    zoomCW = (ImageButton) findViewById(R.id.zoomCW);
    focusCCW = (ImageButton) findViewById(R.id.focusCCW);
    focusCW = (ImageButton) findViewById(R.id.focusCW);

    slideLengthTextView = (TextView) findViewById(R.id.slideAmount);
    panAngleTextView = (TextView) findViewById(R.id.pan);
    tiltAngleTextView = (TextView) findViewById(R.id.tilt);
    zoomTextView = (TextView) findViewById(R.id.zoom);
    focusTextView = (TextView) findViewById(R.id.focus);
    time = (TextView) findViewById(R.id.time);
    waitTime = (TextView) findViewById(R.id.waitTime);

    // Open a time selection panel that allows setting the duration of the movement
    timePanel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ValuePickerDialog timePicker = ValuePickerDialog.newInstance(TIME_PICKER);
        timePicker
            .setValue(keyframe.getDuration())
            .setStepSize(100)
            .setTitle("DURATION")
            .setMessage("Select how long the final timelapse video should last.")
            .setIcon(R.drawable.ic_access_time_white_36dp)
            .setDivider(1000)
            .setUnit("s")
            .setMinimumValue(100);

        timePicker.setListener(KeyframeEditActivity.this);

        timePicker.show(getSupportFragmentManager(), "time_picker_dialog");
      }
    });

    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finishAndSave();
      }
    });

    new IncreaseDecreaseHandler(slideRight, slideLeft, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        keyframe.setSlideLength(keyframe.getSlideLength() + 1);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        keyframe.setSlideLength(keyframe.getSlideLength() - 1);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(panCW, panCCW, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        keyframe.setPanAngle(keyframe.getPanAngle() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        keyframe.setPanAngle(keyframe.getPanAngle() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(tiltCW, tiltCCW, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        keyframe.setTiltAngle(keyframe.getTiltAngle() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        keyframe.setTiltAngle(keyframe.getTiltAngle() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(zoomCW, zoomCCW, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        keyframe.setZoom(keyframe.getZoom() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        keyframe.setZoom(keyframe.getZoom() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(focusCW, focusCCW, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        keyframe.setFocus(keyframe.getFocus() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        keyframe.setFocus(keyframe.getFocus() - 10);
        updateTextViews();
      }
    });

    updateTextViews();
  }

  @Override
  public void onValuePickerValueReceived(int requestCode, int value) {
    if (requestCode == TIME_PICKER) {
      this.keyframe.setDuration(value);
      updateTextViews();
    }
  }

  private void finishAndSave() {
    Intent result = new Intent();

    // Return the settings to the main Activity
    result.putExtra("KEYFRAME_INDEX", keyframeIndex);
    result.putExtra("KEYFRAME_DURATION", keyframe.getDuration());
    result.putExtra("KEYFRAME_SLIDE", keyframe.getSlideLength());
    result.putExtra("KEYFRAME_PAN", keyframe.getPanAngle());
    result.putExtra("KEYFRAME_TILT", keyframe.getTiltAngle());
    result.putExtra("KEYFRAME_ZOOM", keyframe.getZoom());
    result.putExtra("KEYFRAME_FOCUS", keyframe.getFocus());

    setResult(RESULT_OK, result);
    finish();
  }

  @Override
  public void onBackPressed() {
    if (keyframeIndex != -1) {
      finishAndSave();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_keyframe_edit_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.deleteKeyframe) {
      if (keyframeIndex == -1) {
        finish();
      }

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Are you sure?");
      builder.setMessage("This action cannot be reversed. Please be sure before you delete.");

      builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          Intent result = new Intent();
          result.putExtra("KEYFRAME_INDEX", keyframeIndex);
          result.putExtra("DELETE", true);
          setResult(RESULT_OK, result);
          finish();
        }
      });

      builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
      });

      builder.show();

      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  /**
   * Update UI
   */
  private void updateTextViews() {
    this.time.setText(this.keyframe.getFormattedVideoLength());
    this.waitTime.setText(this.keyframe.getFormattedDuration(fps, interval));
    this.slideLengthTextView.setText(this.keyframe.getFormattedSlideLength());
    this.panAngleTextView.setText(this.keyframe.getFormattedPanAngle());
    this.tiltAngleTextView.setText(this.keyframe.getFormattedTiltAngle());
    this.zoomTextView.setText(this.keyframe.getFormattedZoom());
    this.focusTextView.setText(this.keyframe.getFormattedFocus());
  }
}
