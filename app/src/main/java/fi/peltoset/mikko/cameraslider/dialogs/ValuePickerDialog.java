package fi.peltoset.mikko.cameraslider.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import fi.peltoset.mikko.cameraslider.IncreaseDecreaseHandler;
import fi.peltoset.mikko.cameraslider.IncreaseDecreaseHandlerStub;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.interfaces.IncreaseDecreaseListener;
import fi.peltoset.mikko.cameraslider.interfaces.ValuePickerDialogInterface;

public class ValuePickerDialog extends DialogFragment {

  private int requestCode;

  private Button saveButton;
  private Button cancelButton;
  private ImageButton increaseButton;
  private ImageButton decreaseButton;
  private TextView valueTextView;
  private TextView dialogTitle;
  private TextView dialogMessage;
  private ImageView dialogIcon;

  private int value = 1;
  private int minimumValue = Integer.MIN_VALUE;
  private int maximumValue = Integer.MAX_VALUE;
  private int stepSize;
  private double divider = 1;
  private String unit = "";
  private String title = "";
  private String message = "";
  private int icon = 0;

  private ValuePickerDialogInterface listener;

  public ValuePickerDialog() {}

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle data = getArguments();
    this.requestCode = data.getInt("REQUEST_CODE", -1);
  }

  public static ValuePickerDialog newInstance(int requestCode) {
    ValuePickerDialog dialog = new ValuePickerDialog();

    Bundle data = new Bundle();
    data.putInt("REQUEST_CODE", requestCode);
    dialog.setArguments(data);

    return dialog;
  }

  public void setListener(ValuePickerDialogInterface listener) {
    this.listener = listener;
  }

  public ValuePickerDialog setTitle(String title) {
    this.title = title;
    return this;
  }

  public ValuePickerDialog setMessage(String message) {
    this.message = message;
    return this;
  }

  public ValuePickerDialog setIcon(int imageResource) {
    this.icon = imageResource;
    return this;
  }

  public ValuePickerDialog setDivider(double divider) {
    this.divider = divider;
    return this;
  }

  public ValuePickerDialog setUnit(String unit) {
    this.unit = unit;
    return this;
  }

  public ValuePickerDialog setMinimumValue(int minimumValue) {
    this.minimumValue = minimumValue;
    return this;
  }

  public ValuePickerDialog setMaximumValue(int maximumValue) {
    this.maximumValue = maximumValue;
    return this;
  }

  public ValuePickerDialog setStepSize(int stepSize) {
    this.stepSize = stepSize;
    return this;
  }

  public ValuePickerDialog setValue(int value) {
    this.value = value;
    return this;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_value_picker, container);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    saveButton = (Button) view.findViewById(R.id.saveButton);
    cancelButton = (Button) view.findViewById(R.id.cancelButton);
    increaseButton = (ImageButton) view.findViewById(R.id.increaseButton);
    decreaseButton = (ImageButton) view.findViewById(R.id.decreaseButton);
    valueTextView = (TextView) view.findViewById(R.id.value);

    dialogTitle = (TextView) view.findViewById(R.id.dialogTitle);
    dialogMessage = (TextView) view.findViewById(R.id.dialogMessage);
    dialogIcon = (ImageView) view.findViewById(R.id.dialogIcon);

    valueTextView.setText(getFormattedValue());
    dialogTitle.setText(title);
    dialogMessage.setText(message);
    dialogIcon.setImageResource(icon);

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
        listener.onValuePickerValueReceived(requestCode, value);

        dismiss();
      }
    });

    new IncreaseDecreaseHandler(increaseButton, decreaseButton, new IncreaseDecreaseHandlerStub() {
      @Override
      public void onIncrease() {
        if (value + stepSize > maximumValue) {
          return;
        }

        value += stepSize;
        updateTextView();
      }

      @Override
      public void onDecrease() {
        if (value - stepSize < minimumValue) {
          return;
        }

        value -= stepSize;
        updateTextView();
      }
    });
  }

  private void updateTextView() {
    this.valueTextView.setText(getFormattedValue());
  }

  private String getFormattedValue() {
    String formatted = "";

    if (this.divider > 1) {
      DecimalFormat df = new DecimalFormat("0.0");
      df.setRoundingMode(RoundingMode.HALF_UP);

      formatted = df.format(this.value / this.divider);
    } else {
      formatted = Integer.toString(this.value);
    }

    if (!this.unit.equals("")) {
      formatted += " " + this.unit;
    }

    return formatted;
  }
}
