package fi.peltoset.mikko.cameraslider;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KeyframeEditActivity extends AppCompatActivity {

  private LinearLayout timePanel;

  private int slideLength = 0;
  private double panAngle;
  private double tiltAngle;
  private double zoom;
  private double focus;

  private ImageButton slideLeft, slideRight;
  private ImageButton panCCW, panCW;
  private ImageButton tiltCCW, tiltCW;
  private ImageButton zoomCCW, zoomCW;
  private ImageButton focusCCW, focusCW;

  private boolean slideLengthIncrementing, slideLengthDecrementing;
  private boolean panAngleIncrementing, pinAngleDecrementing;
  private boolean tiltAngleIncrementing, tiltAngleDecrementing;
  private boolean zoomIncrementing, zoomDecrementing;
  private boolean focusIncrementing, focusDecrementing;

  private Handler slideLengthHandler = new Handler();
  private Handler panAngleHandler = new Handler();
  private Handler tiltAngleHandler = new Handler();
  private Handler zoomHandler = new Handler();
  private Handler focusHandler = new Handler();

  private TextView slideLengthTextView;
  private TextView panAngleTextView;
  private TextView tiltAngleTextView;
  private TextView zoomTextView;
  private TextView focusTextView;

  private final static int REPEAT_DELAY = 50;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_keyframe_edit);

    Toolbar toolbar = (Toolbar) findViewById(R.id.keyframeToolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    timePanel = (LinearLayout) findViewById(R.id.timePanel);

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

    slideRight.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        slideLengthIncrementing = true;
        slideLengthHandler.post(new RepeatUpdater());
        return false;
      }
    });

    slideRight.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && slideLengthIncrementing) {
          slideLengthIncrementing = false;
        }

        return false;
      }
    });

    slideRight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        increment();
      }
    });

    slideLeft.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        slideLengthDecrementing = true;
        slideLengthHandler.post(new RepeatUpdater());
        return false;
      }
    });

    slideLeft.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && slideLengthDecrementing) {
          slideLengthDecrementing = false;
        }

        return false;
      }
    });

    slideLeft.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        decrement();
      }
    });


    timePanel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(KeyframeEditActivity.this, "Heheheh", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_keyframe_edit_menu, menu);
    return true;
  }

  private void increment() {
    this.slideLength += 1;
    updateTextView();
  }

  private void decrement() {
    this.slideLength -= 1;
    updateTextView();
  }

  private void updateTextView() {
    this.slideLengthTextView.setText(this.slideLength + " mm");
  }

  private class RepeatUpdater implements Runnable {
    @Override
    public void run() {
      if (slideLengthIncrementing) {
        increment();
        slideLengthHandler.postDelayed(new RepeatUpdater(), REPEAT_DELAY);
      } else if (slideLengthDecrementing) {
        decrement();
        slideLengthHandler.postDelayed(new RepeatUpdater(), REPEAT_DELAY);
      }
    }
  }
}
