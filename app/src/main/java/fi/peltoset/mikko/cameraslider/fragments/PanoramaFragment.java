package fi.peltoset.mikko.cameraslider.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import fi.peltoset.mikko.cameraslider.IncreaseDecreaseHandler;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.interfaces.IncreaseDecreaseListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.Helpers;

public class PanoramaFragment extends Fragment {

  private TextView horizontalShiftTextView;
  private TextView verticalShiftTextView;
  private TextView overlapPercentageTextView;
  private TextView focalLengthTextView;

  private ImageButton decreaseHorizontalShift, increaseHorizontalShift;
  private ImageButton decreaseVerticalShift, increaseVerticalShift;
  private ImageButton decreaseOverlapPercentage, increaseOverlapPercentage;
  private ImageButton decreaseFocalLength, increaseFocalLength;

  private LinearLayout overlapPercentageContainer;
  private LinearLayout focalLengthContainer;

  private int horizontalShift = 0;
  private int verticalShift = 0;
  private int overlapPercentage = 30;
  private int focalLength = 200;

  private static final int OVERLAP_PERCENTAGE_PICKER = 10;
  private static final int FOCAL_LENGTH_PICKER = 11;

  public PanoramaFragment() {}

  public static PanoramaFragment newInstance() {
    PanoramaFragment fragment = new PanoramaFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_panorama, container, false);

    horizontalShiftTextView = (TextView) view.findViewById(R.id.horizontalShift);
    verticalShiftTextView = (TextView) view.findViewById(R.id.verticalShift);
    overlapPercentageTextView = (TextView) view.findViewById(R.id.overlapPercentage);
    focalLengthTextView = (TextView) view.findViewById(R.id.focalLength);

    decreaseHorizontalShift = (ImageButton) view.findViewById(R.id.moveLeft);
    increaseHorizontalShift = (ImageButton) view.findViewById(R.id.moveRight);
    decreaseVerticalShift = (ImageButton) view.findViewById(R.id.moveDown);
    increaseVerticalShift = (ImageButton) view.findViewById(R.id.moveUp);
    decreaseOverlapPercentage = (ImageButton) view.findViewById(R.id.decreaseOverlap);
    increaseOverlapPercentage = (ImageButton) view.findViewById(R.id.increaseOverlap);
    decreaseFocalLength = (ImageButton) view.findViewById(R.id.decreaseFocalLength);
    increaseFocalLength = (ImageButton) view.findViewById(R.id.increaseFocalLength);

    new IncreaseDecreaseHandler(increaseHorizontalShift, decreaseHorizontalShift, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        horizontalShift += 10;
        horizontalShiftTextView.setText(Helpers.formatAngle(horizontalShift) + " °");
      }

      @Override
      public void onDecrease() {
        horizontalShift -= 10;
        horizontalShiftTextView.setText(Helpers.formatAngle(horizontalShift) + " °");
      }
    });

    new IncreaseDecreaseHandler(increaseVerticalShift, decreaseVerticalShift, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        verticalShift += 10;
        verticalShiftTextView.setText(Helpers.formatAngle(verticalShift) + " °");
      }

      @Override
      public void onDecrease() {
        verticalShift -= 10;
        verticalShiftTextView.setText(Helpers.formatAngle(verticalShift) + " °");
      }
    });

    new IncreaseDecreaseHandler(increaseOverlapPercentage, decreaseOverlapPercentage, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        if (overlapPercentage >= 99) {
          return;
        }

        overlapPercentage += 1;
        overlapPercentageTextView.setText(overlapPercentage + " %");
      }

      @Override
      public void onDecrease() {
        if (overlapPercentage <= 0) {
          return;
        }

        overlapPercentage -= 1;
        overlapPercentageTextView.setText(overlapPercentage + " %");
      }
    });

    new IncreaseDecreaseHandler(increaseFocalLength, decreaseFocalLength, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        focalLength += 1;
        focalLengthTextView.setText(focalLength + " mm");
      }

      @Override
      public void onDecrease() {
        if (focalLength <= 1) {
          return;
        }

        focalLength -= 1;
        focalLengthTextView.setText(focalLength + " mm");
      }
    });

    return view;
  }
}
