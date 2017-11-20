package fi.peltoset.mikko.cameraslider.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import fi.peltoset.mikko.cameraslider.IncreaseDecreaseHandler;
import fi.peltoset.mikko.cameraslider.IncreaseDecreaseHandlerStub;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.miscellaneous.KeyframePOJO;
import fi.peltoset.mikko.cameraslider.miscellaneous.RotationDirection;


public class ManualModeFragment extends Fragment {

  private KeyframePOJO currentPosition = new KeyframePOJO();
  private KeyframePOJO homePosition = new KeyframePOJO();

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

  private Button goHomeButton;
  private Button setHomeButton;
  private Button resetHomeButton;

  private LinearLayout controlsOverlay;

  private ManualModeListener listener;
  private EventBus eventBus;

  public class ManualMoveInstructions {
    public RotationDirection slide = RotationDirection.STOP;
    public RotationDirection pan = RotationDirection.STOP;
    public RotationDirection tilt = RotationDirection.STOP;
    public RotationDirection zoom = RotationDirection.STOP;
    public RotationDirection focus = RotationDirection.STOP;
  }

  ManualMoveInstructions manualMoveInstructions = new ManualMoveInstructions();

  public interface ManualModeListener {
      void setHome();
    void goHome();
    void resetHome();
    void moveManually(ManualMoveInstructions instructions);
  }

  public ManualModeFragment() {}

  public static ManualModeFragment newInstance() {
    ManualModeFragment fragment = new ManualModeFragment();
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // Verify that the Activity implements the ManualModeListener interface
    if (context instanceof ManualModeListener) {
      listener = (ManualModeListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement ManualModeListener");
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRetainInstance(true);

    if (savedInstanceState != null) {
      currentPosition = savedInstanceState.getParcelable("currentPosition");
    }

    eventBus = EventBus.getDefault();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putParcelable("currentPosition", currentPosition);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_manual_mode, container, false);

    slideLeft = (ImageButton) view.findViewById(R.id.slideLeft);
    slideRight = (ImageButton) view.findViewById(R.id.slideRight);
    panCCW = (ImageButton) view.findViewById(R.id.panCCW);
    panCW = (ImageButton) view.findViewById(R.id.panCW);
    tiltCCW = (ImageButton) view.findViewById(R.id.tiltCCW);
    tiltCW = (ImageButton) view.findViewById(R.id.tiltCW);
    zoomCCW = (ImageButton) view.findViewById(R.id.zoomCCW);
    zoomCW = (ImageButton) view.findViewById(R.id.zoomCW);
    focusCCW = (ImageButton) view.findViewById(R.id.focusCCW);
    focusCW = (ImageButton) view.findViewById(R.id.focusCW);

    slideLengthTextView = (TextView) view.findViewById(R.id.slideAmount);
    panAngleTextView = (TextView) view.findViewById(R.id.pan);
    tiltAngleTextView = (TextView) view.findViewById(R.id.tilt);
    zoomTextView = (TextView) view.findViewById(R.id.zoom);
    focusTextView = (TextView) view.findViewById(R.id.focus);

    goHomeButton = (Button) view.findViewById(R.id.goHomeButton);
    setHomeButton = (Button) view.findViewById(R.id.setHomeButton);
    resetHomeButton = (Button) view.findViewById(R.id.resetHomeButton);

    controlsOverlay = (LinearLayout) view.findViewById(R.id.controlsOverlay);

    // Discard the current position and move to the last set home position
    goHomeButton.setOnClickListener(v -> {
      currentPosition = new KeyframePOJO();
      currentPosition.setSlideLength(homePosition.getSlideLength());
      currentPosition.setPanAngle(homePosition.getPanAngle());
      currentPosition.setTiltAngle(homePosition.getTiltAngle());
      currentPosition.setZoom(homePosition.getZoom());
      currentPosition.setFocus(homePosition.getFocus());
      updateTextViews();

      final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle);
      progressDialog.setMessage("Moving to home position...");
      progressDialog.show();

      listener.goHome();

      new Handler().postDelayed(() -> progressDialog.cancel(), 3000);
    });

    // Set the current position as the home position
    setHomeButton.setOnClickListener(v -> {
      homePosition = new KeyframePOJO();
      homePosition.setSlideLength(currentPosition.getSlideLength());
      homePosition.setPanAngle(currentPosition.getPanAngle());
      homePosition.setTiltAngle(currentPosition.getTiltAngle());
      homePosition.setZoom(currentPosition.getZoom());
      homePosition.setFocus(currentPosition.getFocus());

      currentPosition = new KeyframePOJO();

      listener.setHome();

      updateTextViews();
    });

    // Reset the home position to the initial position defined by the Camera Slider
    resetHomeButton.setOnClickListener(v -> {
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
      alertDialogBuilder
          .setMessage("Are you sure you want to reset the positon?")
          .setPositiveButton("Reset", (dialog, which) -> {
            homePosition = new KeyframePOJO();
            currentPosition = new KeyframePOJO();

            final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle);
            progressDialog.setMessage("Moving to initial home position...");
            progressDialog.show();

            listener.resetHome();

            new Handler().postDelayed(() -> progressDialog.cancel(), 4000);
          })
          .setNegativeButton("Cancel", (dialog, which) -> {

          });

      alertDialogBuilder.create().show();
    });

    new IncreaseDecreaseHandler(slideRight, slideLeft, new IncreaseDecreaseHandlerStub() {
      @Override
      public void onStop() {
        manualMoveInstructions.slide = RotationDirection.STOP;
        listener.moveManually(manualMoveInstructions);
      }

      @Override
      public void onIncrease() {
        manualMoveInstructions.slide = RotationDirection.CW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setSlideLength(currentPosition.getSlideLength() + 1);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        manualMoveInstructions.slide = RotationDirection.CCW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setSlideLength(currentPosition.getSlideLength() - 1);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(panCW, panCCW, new IncreaseDecreaseHandlerStub() {
      @Override
      public void onStop() {
        manualMoveInstructions.pan = RotationDirection.STOP;
        listener.moveManually(manualMoveInstructions);
      }

      @Override
      public void onIncrease() {
        manualMoveInstructions.pan = RotationDirection.CW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setPanAngle(currentPosition.getPanAngle() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        manualMoveInstructions.pan = RotationDirection.CCW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setPanAngle(currentPosition.getPanAngle() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(tiltCW, tiltCCW, new IncreaseDecreaseHandlerStub() {
      @Override
      public void onStop() {
        manualMoveInstructions.tilt = RotationDirection.STOP;
        listener.moveManually(manualMoveInstructions);
      }

      @Override
      public void onIncrease() {
        manualMoveInstructions.tilt = RotationDirection.CW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setTiltAngle(currentPosition.getTiltAngle() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        manualMoveInstructions.tilt = RotationDirection.CCW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setTiltAngle(currentPosition.getTiltAngle() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(zoomCW, zoomCCW, new IncreaseDecreaseHandlerStub() {
      @Override
      public void onStop() {
        manualMoveInstructions.zoom = RotationDirection.STOP;
        listener.moveManually(manualMoveInstructions);
      }

      @Override
      public void onIncrease() {
        manualMoveInstructions.zoom = RotationDirection.CW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setZoom(currentPosition.getZoom() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        manualMoveInstructions.zoom = RotationDirection.CCW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setZoom(currentPosition.getZoom() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(focusCW, focusCCW, new IncreaseDecreaseHandlerStub() {
      @Override
      public void onStop() {
        manualMoveInstructions.focus = RotationDirection.STOP;
        listener.moveManually(manualMoveInstructions);
      }

      @Override
      public void onIncrease() {
        manualMoveInstructions.focus = RotationDirection.CW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setFocus(currentPosition.getFocus() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        manualMoveInstructions.focus = RotationDirection.CCW;
        listener.moveManually(manualMoveInstructions);
        currentPosition.setFocus(currentPosition.getFocus() - 10);
        updateTextViews();
      }
    });

    updateTextViews();

    return view;
  }

  private void updateTextViews() {
    this.slideLengthTextView.setText(this.currentPosition.getFormattedSlideLength());
    this.panAngleTextView.setText(this.currentPosition.getFormattedPanAngle());
    this.tiltAngleTextView.setText(this.currentPosition.getFormattedTiltAngle());
    this.zoomTextView.setText(this.currentPosition.getFormattedZoom());
    this.focusTextView.setText(this.currentPosition.getFormattedFocus());
  }
}
