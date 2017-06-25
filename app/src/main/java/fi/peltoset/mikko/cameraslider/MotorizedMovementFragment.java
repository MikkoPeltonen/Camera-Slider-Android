package fi.peltoset.mikko.cameraslider;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class MotorizedMovementFragment extends Fragment {

  private static final int CREATE_NEW_KEYFRAME = 1;
  private static final int EDIT_KEYFRAME = 2;
  private static final int INTERVAL_PICKER = 3;

  private int interval = 1000;

  private FloatingActionButton startMovement;
  private FloatingActionButton addKeyframe;
  private RecyclerView keyframesRecyclerView;
  private LinearLayout timelapseRow;
  private TextView finalVideoLength;
  private TextView videoFPS;
  private TextView totalFramesCaptured;
  private LinearLayout videoFPSContainer;
  private LinearLayout photoIntervalContainer;
  private TextView photoInterval;

  private RecyclerView.Adapter keyframeAdapter;
  private RecyclerView.LayoutManager keyframeLayoutManager;

  private enum Mode {
    TIMELAPSE, VIDEO
  }

  // Frame rates: 23,976; 24; 25; 29,97; 30; 50; 59,94; 60

  private Mode operationMode = Mode.TIMELAPSE;

  private ArrayList<KeyframePOJO> keyframes = new ArrayList<>();

  public MotorizedMovementFragment() {
  }

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

    addKeyframe.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getActivity(), KeyframeEditActivity.class);
        startActivityForResult(intent, CREATE_NEW_KEYFRAME);
      }
    });

    videoFPSContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getContext(), "Set FPS", Toast.LENGTH_SHORT).show();
      }
    });

    photoIntervalContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        IntervalPickerDialog intervalPicker = IntervalPickerDialog.newInstance(interval);
        intervalPicker.show(getFragmentManager(), "interval_picker_fragment");
        intervalPicker.setTargetFragment(MotorizedMovementFragment.this, INTERVAL_PICKER);
      }
    });

    // Set up RecyclerView with data
    keyframesRecyclerView.setHasFixedSize(true);
    keyframeLayoutManager = new LinearLayoutManager(getContext());
    keyframesRecyclerView.setLayoutManager(keyframeLayoutManager);

    keyframes.add(new KeyframePOJO(5400, 525, 650, 430, 300, 720));
    keyframes.add(new KeyframePOJO(10000, 302, 53, -63, 0, 112));
    keyframes.add(new KeyframePOJO(6700, 0, -20, 41, -70, 50));


    keyframeAdapter = new KeyframeAdapter(keyframes);
    keyframesRecyclerView.setAdapter(keyframeAdapter);

    keyframesRecyclerView.addOnItemTouchListener(
        new RecyclerItemClickListener(getContext(), keyframesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
          @Override
          public void onItemClick(View view, int position) {
            KeyframePOJO keyframe = keyframes.get(position);

            Intent intent = new Intent(getActivity(), KeyframeEditActivity.class);
            intent.putExtra("KEYFRAME_INDEX", position);
            intent.putExtra("KEYFRAME_DURATION", keyframe.getDuration());
            intent.putExtra("KEYFRAME_SLIDE", keyframe.getSlideLength());
            intent.putExtra("KEYFRAME_PAN", keyframe.getPanAngle());
            intent.putExtra("KEYFRAME_TILT", keyframe.getTiltAngle());
            intent.putExtra("KEYFRAME_ZOOM", keyframe.getZoom());
            intent.putExtra("KEYFRAME_FOCUS", keyframe.getFocus());

            startActivityForResult(intent, EDIT_KEYFRAME);

            Log.d("cs", position + "");
          }

          @Override
          public void onLongItemClick(View view, int position) {}
        })
    );

    return view;
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

      if (requestCode == CREATE_NEW_KEYFRAME && resultCode == RESULT_OK) {
        keyframes.add(keyframe);
        keyframeAdapter.notifyItemInserted(keyframes.size() - 1);
      } else if (requestCode == EDIT_KEYFRAME && resultCode == RESULT_OK && index != -1) {
        keyframes.set(index, keyframe);
        keyframeAdapter.notifyItemChanged(index);
      }
    } else if (requestCode == INTERVAL_PICKER) {
      MotorizedMovementFragment.this.interval = data.getIntExtra("INTERVAL", 1000);
      updateIntervalTime();
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

  private void updateIntervalTime() {
    DecimalFormat df = new DecimalFormat("0.0");
    df.setRoundingMode(RoundingMode.HALF_UP);
    String time = df.format(this.interval / 1000.0);
    this.photoInterval.setText(time + " s");
  }
}
