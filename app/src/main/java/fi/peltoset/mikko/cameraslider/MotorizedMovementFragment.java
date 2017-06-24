package fi.peltoset.mikko.cameraslider;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;

public class MotorizedMovementFragment extends Fragment {

  private FloatingActionButton startMovement;
  private FloatingActionButton addKeyframe;
  private RecyclerView keyframesRecyclerView;
  private LinearLayout timelapseRow;
  private TextView finalVideoLength;
  private TextView videoFPS;
  private TextView totalFramesCaptured;
  private LinearLayout videoFPSContainer;
  private LinearLayout timeContainer;


  private RecyclerView.Adapter keyframeAdapter;
  private RecyclerView.LayoutManager keyframeLayoutManager;

  private enum Mode {
    TIMELAPSE, VIDEO
  }

  // Frame rates: 23,976; 24; 25; 29,97; 30; 50; 59,94; 60

  private Mode operationMode = Mode.TIMELAPSE;

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
    timeContainer = (LinearLayout) view.findViewById(R.id.timeContainer);

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
        startActivity(intent);
      }
    });

    timeContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

      }
    });

    videoFPSContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

      }
    });

    // Set up RecyclerView with data
    keyframesRecyclerView.setHasFixedSize(true);
    keyframeLayoutManager = new LinearLayoutManager(getContext());
    keyframesRecyclerView.setLayoutManager(keyframeLayoutManager);

    ArrayList<KeyframePOJO> keyframes = new ArrayList<>();
    keyframes.add(new KeyframePOJO(3540, 52.5, 6.5, 4.3, 30.0, 7.2));
    keyframes.add(new KeyframePOJO(1550, 30.2, 5.3, -6.3, 0.0, 11.2));
    keyframes.add(new KeyframePOJO(2000, 0.0, -2.0, 4.1, -7.0, 5.0));
    keyframes.add(new KeyframePOJO(3540, 52.5, 6.5, 4.3, 30.0, 7.2));
    keyframes.add(new KeyframePOJO(1550, 30.2, 5.3, -6.3, 0.0, 11.2));
    keyframes.add(new KeyframePOJO(2000, 0.0, -2.0, 4.1, -7.0, 5.0));
    keyframes.add(new KeyframePOJO(3540, 52.5, 6.5, 4.3, 30.0, 7.2));
    keyframes.add(new KeyframePOJO(1550, 30.2, 5.3, -6.3, 0.0, 11.2));
    keyframes.add(new KeyframePOJO(2000, 0.0, -2.0, 4.1, -7.0, 5.0));


    keyframeAdapter = new KeyframeAdapter(keyframes);
    keyframesRecyclerView.setAdapter(keyframeAdapter);

    return view;
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
}
