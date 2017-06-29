package fi.peltoset.mikko.cameraslider;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class MotorizedMovementFragment extends Fragment implements ValuePickerDialogInterface {

  // Activity result codes
  private static final int CREATE_NEW_KEYFRAME = 1;
  private static final int EDIT_KEYFRAME = 2;
  private static final int INTERVAL_PICKER = 3;
  private static final int FPS_PICKER = 4;

  private int interval = 1000; // milliseconds
  private int fps = 30000; // thousandths

  private FloatingActionButton startMovement;
  private FloatingActionButton addKeyframe;
  private RecyclerView keyframesRecyclerView;
  private LinearLayout timelapseRow;
  private TextView finalVideoLength;
  private TextView videoFPS;
  private TextView totalWaitTime;
  private TextView totalFramesCaptured;
  private LinearLayout videoFPSContainer;
  private LinearLayout photoIntervalContainer;
  private TextView photoInterval;

  private RecyclerView.Adapter keyframeAdapter;
  private RecyclerView.LayoutManager keyframeLayoutManager;
  private RecyclerViewFpsIntervalListener adapterListener;

  private enum Mode {
    TIMELAPSE, VIDEO
  }

  private Mode operationMode = Mode.TIMELAPSE;

  // All keyframes in the motion
  private ArrayList<KeyframePOJO> keyframes = new ArrayList<>();

  public MotorizedMovementFragment() {}

  public static MotorizedMovementFragment newInstance() {
    MotorizedMovementFragment fragment = new MotorizedMovementFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt("INTERVAL", interval);
    outState.putInt("FPS", fps);

    super.onSaveInstanceState(outState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_motorized_movement, container, false);

    // Find items from layout
    startMovement = (FloatingActionButton) view.findViewById(R.id.startMovement);
    addKeyframe = (FloatingActionButton) view.findViewById(R.id.addKeyframe);
    keyframesRecyclerView = (RecyclerView) view.findViewById(R.id.keyframes);
    timelapseRow = (LinearLayout) view.findViewById(R.id.timelapseRow);
    finalVideoLength = (TextView) view.findViewById(R.id.finalVideoLength);
    videoFPS = (TextView) view.findViewById(R.id.videoFPS);
    totalFramesCaptured = (TextView) view.findViewById(R.id.totalFramesCaptured);
    videoFPSContainer = (LinearLayout) view.findViewById(R.id.videoFPSContainer);
    photoIntervalContainer = (LinearLayout) view.findViewById(R.id.photoIntervalContainer);
    photoInterval = (TextView) view.findViewById(R.id.photoInterval);
    totalWaitTime = (TextView) view.findViewById(R.id.totalRunningTime);

    // Handle start button click
    startMovement.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose action");
        builder.setMessage("Do you wish to move through the sequence first to quickly preview the timelapse?");

        builder.setPositiveButton("Run", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent recordingRunningActivity = new Intent(getActivity(), RecordingRunningActivity.class);
            startActivity(recordingRunningActivity);
          }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {

          }
        });

        builder.setNegativeButton("Preview", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {

          }
        });

        builder.create().show();
      }
    });

    // Start a new Activity for adding a new keyframe
    addKeyframe.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getActivity(), KeyframeEditActivity.class);
        intent.putExtra("FPS", fps);
        intent.putExtra("INTERVAL", interval);
        startActivityForResult(intent, CREATE_NEW_KEYFRAME);
      }
    });

    // Open a dialog for choosing the video FPS
    videoFPSContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FPSPickerDialog fpsPicker = FPSPickerDialog.newInstance();
        fpsPicker.setFPS(fps);
        fpsPicker.show(getFragmentManager(), "fps_picker_dialog");
        fpsPicker.setTargetFragment(MotorizedMovementFragment.this, FPS_PICKER);
      }
    });

    // Open a dialog for choosing the interval photos are taken
    photoIntervalContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ValuePickerDialog intervalPicker = ValuePickerDialog.newInstance(INTERVAL_PICKER);
        intervalPicker
            .setValue(interval)
            .setStepSize(100)
            .setTitle("Interval")
            .setMessage("Select how often the camera should take a picture.")
            .setIcon(R.drawable.ic_av_timer_white_36dp)
            .setDivider(1000)
            .setUnit("s")
            .setMinimumValue(100);

        intervalPicker.setListener(MotorizedMovementFragment.this);

        intervalPicker.show(getFragmentManager(), "interval_picker_fragment");
        intervalPicker.setTargetFragment(MotorizedMovementFragment.this, INTERVAL_PICKER);
      }
    });

    // Set up RecyclerView with data
    keyframesRecyclerView.setHasFixedSize(true);
    keyframeLayoutManager = new LinearLayoutManager(getContext());
    keyframesRecyclerView.setLayoutManager(keyframeLayoutManager);

    // Sample keyframes, to be removed
    keyframes.add(new KeyframePOJO(5400, 525, 650, 430, 300, 720));
    keyframes.add(new KeyframePOJO(10000, 302, 50, -70, 0, 110));
    keyframes.add(new KeyframePOJO(6700, 0, -20, 40, -70, 50));

    keyframeAdapter = new KeyframeAdapter(keyframes, fps, interval);
    keyframesRecyclerView.setAdapter(keyframeAdapter);

    // RecyclerViewFpsIntervalListener allows to update FPS/interval changes to the items in the
    // RecyclerView so that the times are correctly displayed
    adapterListener = (RecyclerViewFpsIntervalListener) keyframeAdapter;

    // Handle opening an edit dialog for keyframes when they are clicked in the RecyclerView list
    keyframesRecyclerView.addOnItemTouchListener(
        new RecyclerItemClickListener(getContext(), keyframesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
          @Override
          public void onItemClick(View view, int position) {
            KeyframePOJO keyframe = keyframes.get(position);

            Intent intent = new Intent(getActivity(), KeyframeEditActivity.class);
            intent.putExtra("FPS", fps);
            intent.putExtra("INTERVAL", interval);
            intent.putExtra("KEYFRAME_INDEX", position);
            intent.putExtra("KEYFRAME_DURATION", keyframe.getDuration());
            intent.putExtra("KEYFRAME_SLIDE", keyframe.getSlideLength());
            intent.putExtra("KEYFRAME_PAN", keyframe.getPanAngle());
            intent.putExtra("KEYFRAME_TILT", keyframe.getTiltAngle());
            intent.putExtra("KEYFRAME_ZOOM", keyframe.getZoom());
            intent.putExtra("KEYFRAME_FOCUS", keyframe.getFocus());

            startActivityForResult(intent, EDIT_KEYFRAME);
          }

          @Override
          public void onLongItemClick(View view, int position) {}
        })
    );

    // THIS SHOULD NOT BE HERE AFTER REMOVING INITIAL KEYFRAMES
    updateTotalTimeAndFrames();
    updateIntervalTime();

    if (savedInstanceState != null) {
//      interval = savedInstanceState.getInt("INTERVAL");
//      fps = savedInstanceState.getInt("FPS");
//
//      updateIntervalTime();
//      updateTotalTimeAndFrames();
//      updateFPS();
    }

    return view;
  }

  /**
   * When FPS or interval changes, update the total time it is going to take for the timelapse to
   * finish and show the picture count that needs to be taken.
   */
  private void updateTotalTimeAndFrames() {
    // Calculate total duration, in milliseconds
    int totalTime = 0;
    for (KeyframePOJO keyframePOJO : keyframes) {
      totalTime += keyframePOJO.getDuration();
    }

    DecimalFormat df = new DecimalFormat("0.0");
    df.setRoundingMode(RoundingMode.HALF_UP);

    // Final video length
    finalVideoLength.setText(df.format(totalTime / 1000.0) + " s");

    // Total number of frames
    totalFramesCaptured.setText(Integer.toString((int) (totalTime / 1000.0 * fps / 1000.0)));

    // Time it takes to take all the frames, given the interval
    totalTime = (int) (totalTime / 1000.0 * fps / 1000.0 * interval / 1000.0);

    // Display type, depending on the length. Either hours and minutes or minutes and seconds for
    // shorter periods.
    String timeDisplay;
    if (totalTime >= 3600) {
      timeDisplay = (totalTime / 3600) + " h " + (int) (totalTime % 3600 / 3600.0 * 60) + " min";
    } else {
      timeDisplay = (totalTime / 60) + " min " + (totalTime % 60) + " s";
    }

    totalWaitTime.setText(timeDisplay);

    // Update adapter so that each item gets the right timing info
    adapterListener.setFpsAndInterval(fps, interval);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (data == null) {
      return;
    }

    // Handle Activity responses for creating and updating keyframes
    if (requestCode == CREATE_NEW_KEYFRAME || requestCode == EDIT_KEYFRAME) {
      int index = data.getIntExtra("KEYFRAME_INDEX", -1);
      int duration = data.getIntExtra("KEYFRAME_DURATION", 0);
      int slide = data.getIntExtra("KEYFRAME_SLIDE", 0);
      int pan = data.getIntExtra("KEYFRAME_PAN", 0);
      int tilt = data.getIntExtra("KEYFRAME_TILT", 0);
      int zoom = data.getIntExtra("KEYFRAME_ZOOM", 0);
      int focus = data.getIntExtra("KEYFRAME_FOCUS", 0);

      KeyframePOJO keyframe = new KeyframePOJO(duration, slide, pan, tilt, zoom, focus);

      // Add or update existing
      if (requestCode == CREATE_NEW_KEYFRAME && resultCode == RESULT_OK) {
        keyframes.add(keyframe);
        keyframeAdapter.notifyItemInserted(keyframes.size() - 1);
      } else if (requestCode == EDIT_KEYFRAME && resultCode == RESULT_OK && index != -1) {
        if (data.getBooleanExtra("DELETE", false)) {
          keyframes.remove(index);
          keyframeAdapter.notifyItemRemoved(index);
        } else {
          keyframes.set(index, keyframe);
          keyframeAdapter.notifyItemChanged(index);
        }
      }

      updateTotalTimeAndFrames();
    } else if (requestCode == FPS_PICKER) {
      // Handle FPS dialog result
      fps = data.getIntExtra("FPS", 30000);
      updateFPS();
      updateTotalTimeAndFrames();
    }
  }

  @Override
  public void onValuePickerValueReceived(int requestCode, int value) {
    if (requestCode == INTERVAL_PICKER) {
      this.interval = value;
      updateIntervalTime();
      updateTotalTimeAndFrames();
    }
  }

  /**
   * Change operation mode from timelapse to video and vice versa
   */
  private void toggleMode() {
    this.operationMode = this.operationMode == Mode.TIMELAPSE ? Mode.VIDEO : Mode.TIMELAPSE;

    if (this.operationMode == Mode.TIMELAPSE) {
      timelapseRow.setVisibility(View.VISIBLE);
      finalVideoLength.setVisibility(View.VISIBLE);
    } else {
      timelapseRow.setVisibility(View.INVISIBLE);
      finalVideoLength.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * Add options menu
   *
   * @param menu
   * @param inflater
   */
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_motorized_movement_options_menu, menu);
  }

  /**
   * Handle options menu
   *
   * @param item
   * @return
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.timelapseSwitch:
        toggleMode();
        item.setChecked(!item.isChecked());
        return true;
      default:
        return false;
    }
  }

  /**
   * Update the interval display
   */
  private void updateIntervalTime() {
    DecimalFormat df = new DecimalFormat("0.0");
    df.setRoundingMode(RoundingMode.HALF_UP);
    String time = df.format(this.interval / 1000.0);
    this.photoInterval.setText(time + " s");
  }

  /**
   * Update the FPS display
   */
  private void updateFPS() {
    DecimalFormat df = new DecimalFormat("0.###");
    df.setRoundingMode(RoundingMode.HALF_UP);
    this.videoFPS.setText(df.format(this.fps / 1000.0));
  }
}
