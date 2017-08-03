package fi.peltoset.mikko.cameraslider.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import fi.peltoset.mikko.cameraslider.IncreaseDecreaseHandler;
import fi.peltoset.mikko.cameraslider.ManualModeIncreaseDecreaseHandler;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.eventbus.TestEvent;
import fi.peltoset.mikko.cameraslider.interfaces.IncreaseDecreaseListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.KeyframePOJO;


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

  private ManualModeListener listener;

  public interface ManualModeListener {
    void setHome(KeyframePOJO home);
    void goHome();
    void resetHome();
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
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
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

    // Discard the current position and move to the last set home position
    goHomeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
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

        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            progressDialog.cancel();
          }
        }, 3000);
      }
    });

    // Set the current position as the home position
    setHomeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        homePosition = new KeyframePOJO();
        homePosition.setSlideLength(currentPosition.getSlideLength());
        homePosition.setPanAngle(currentPosition.getPanAngle());
        homePosition.setTiltAngle(currentPosition.getTiltAngle());
        homePosition.setZoom(currentPosition.getZoom());
        homePosition.setFocus(currentPosition.getFocus());

        currentPosition = new KeyframePOJO();

//        listener.setHome(homePosition);
        EventBus.getDefault().post(new TestEvent());

        updateTextViews();
      }
    });

    // Reset the home position to the initial position defined by the Camera Slider
    resetHomeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder
            .setMessage("Are you sure you want to reset the positon?")
            .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                homePosition = new KeyframePOJO();
                currentPosition = new KeyframePOJO();

                final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle);
                progressDialog.setMessage("Moving to initial home position...");
                progressDialog.show();

                listener.resetHome();

                new Handler().postDelayed(new Runnable() {
                  @Override
                  public void run() {
                    progressDialog.cancel();
                  }
                }, 4000);
              }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

              }
            });

        alertDialogBuilder.create().show();
      }
    });

    new ManualModeIncreaseDecreaseHandler(slideRight, slideLeft, new ManualModeIncreaseDecreaseHandler.ManualModeIncreaseDecreaseListener() {
      @Override
      public void onIncreaseButtonStateChange(boolean pressed) {
        Toast.makeText(getContext(), "increase " + pressed, Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onDecreaseButtonStateChange(boolean pressed) {
        Toast.makeText(getContext(), "decrease " + pressed, Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onIncrease() {
        currentPosition.setSlideLength(currentPosition.getSlideLength() + 1);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        currentPosition.setSlideLength(currentPosition.getSlideLength() - 1);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(panCW, panCCW, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        currentPosition.setPanAngle(currentPosition.getPanAngle() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        currentPosition.setPanAngle(currentPosition.getPanAngle() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(tiltCW, tiltCCW, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        currentPosition.setTiltAngle(currentPosition.getTiltAngle() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        currentPosition.setTiltAngle(currentPosition.getTiltAngle() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(zoomCW, zoomCCW, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        currentPosition.setZoom(currentPosition.getZoom() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
        currentPosition.setZoom(currentPosition.getZoom() - 10);
        updateTextViews();
      }
    });

    new IncreaseDecreaseHandler(focusCW, focusCCW, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        currentPosition.setFocus(currentPosition.getFocus() + 10);
        updateTextViews();
      }

      @Override
      public void onDecrease() {
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
