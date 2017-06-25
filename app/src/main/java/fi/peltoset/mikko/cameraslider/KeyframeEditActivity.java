package fi.peltoset.mikko.cameraslider;

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
import android.widget.Toast;


public class KeyframeEditActivity extends AppCompatActivity {

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_keyframe_edit);

    Intent intent = getIntent();

    keyframeIndex = intent.getIntExtra("KEYFRAME_INDEX", -1);
    keyframe.setDuration(intent.getIntExtra("KEYFRAME_DURATION", 0));
    keyframe.setSlideLength(intent.getIntExtra("KEYFRAME_SLIDE", 0));
    keyframe.setPanAngle(intent.getIntExtra("KEYFRAME_PAN", 0));
    keyframe.setTiltAngle(intent.getIntExtra("KEYFRAME_TILT", 0));
    keyframe.setZoom(intent.getIntExtra("KEYFRAME_ZOOM", 0));
    keyframe.setFocus(intent.getIntExtra("KEYFRAME_FOCUS", 0));

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

    // Open a time selection panel that allows setting the duration of the movement
    timePanel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(KeyframeEditActivity.this, "Heheheh", Toast.LENGTH_SHORT).show();
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
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Are you sure?");
      builder.setMessage("This action cannot be reversed. Please be sure before you delete.");

      builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

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
    this.time.setText(this.keyframe.getFormattedDuration());
    this.slideLengthTextView.setText(this.keyframe.getFormattedSlideLength());
    this.panAngleTextView.setText(this.keyframe.getFormattedPanAngle());
    this.tiltAngleTextView.setText(this.keyframe.getFormattedTiltAngle());
    this.zoomTextView.setText(this.keyframe.getFormattedZoom());
    this.focusTextView.setText(this.keyframe.getFormattedFocus());
  }
}
