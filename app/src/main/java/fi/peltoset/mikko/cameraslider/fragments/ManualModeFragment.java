package fi.peltoset.mikko.cameraslider.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import fi.peltoset.mikko.cameraslider.IncreaseDecreaseHandler;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.interfaces.IncreaseDecreaseListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.KeyframePOJO;


public class ManualModeFragment extends Fragment {

  private KeyframePOJO keyframe = new KeyframePOJO();

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
  
  public ManualModeFragment() {}

  public static ManualModeFragment newInstance() {
    ManualModeFragment fragment = new ManualModeFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRetainInstance(true);

    if (savedInstanceState != null) {
      keyframe = savedInstanceState.getParcelable("keyframe");
    }
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

//    outState.putParcelable("keyframe", keyframe);
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

    return view;
  }

  private void updateTextViews() {
    this.slideLengthTextView.setText(this.keyframe.getFormattedSlideLength());
    this.panAngleTextView.setText(this.keyframe.getFormattedPanAngle());
    this.tiltAngleTextView.setText(this.keyframe.getFormattedTiltAngle());
    this.zoomTextView.setText(this.keyframe.getFormattedZoom());
    this.focusTextView.setText(this.keyframe.getFormattedFocus());
  }
}
