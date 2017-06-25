package fi.peltoset.mikko.cameraslider;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import static android.app.Activity.RESULT_OK;

public class IntervalPickerDialog extends DialogFragment {

  private Button saveButton;
  private Button cancelButton;
  private ImageButton increaseButton;
  private ImageButton decreaseButton;
  private TextView intervalTextView;

  private int interval = 1000;

  public IntervalPickerDialog() {}

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle arguments = getArguments();

    this.interval = arguments.getInt("INTERVAL", 1);
  }

  public static IntervalPickerDialog newInstance(int interval) {
    IntervalPickerDialog dialog = new IntervalPickerDialog();

    Bundle arguments = new Bundle();
    arguments.putInt("INTERVAL", interval);

    dialog.setArguments(arguments);

    return dialog;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_interval_picker, container);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    saveButton = (Button) view.findViewById(R.id.saveButton);
    cancelButton = (Button) view.findViewById(R.id.cancelButton);
    increaseButton = (ImageButton) view.findViewById(R.id.increaseButton);
    decreaseButton = (ImageButton) view.findViewById(R.id.decreaseButton);
    intervalTextView = (TextView) view.findViewById(R.id.interval);

    intervalTextView.setText(getFormattedInterval());

    // Action handler for cancel button
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    // Action handler for save button
    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent response = new Intent();
        response.putExtra("INTERVAL", interval);
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, response);

        dismiss();
      }
    });

    new IncreaseDecreaseHandler(increaseButton, decreaseButton, new IncreaseDecreaseListener() {
      @Override
      public void onIncrease() {
        IntervalPickerDialog.this.interval += 100;
        updateTextView();
      }

      @Override
      public void onDecrease() {
        if (IntervalPickerDialog.this.interval - 100 < 100) {
          return;
        }

        IntervalPickerDialog.this.interval -= 100;
        updateTextView();
      }
    });
  }

  private void updateTextView() {
    this.intervalTextView.setText(getFormattedInterval());
  }

  private String getFormattedInterval() {
    DecimalFormat df = new DecimalFormat("0.0");
    df.setRoundingMode(RoundingMode.HALF_UP);
    return df.format(interval / 1000.0) + " s";
  }
}
