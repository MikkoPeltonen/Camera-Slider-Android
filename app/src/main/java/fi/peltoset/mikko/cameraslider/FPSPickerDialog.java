package fi.peltoset.mikko.cameraslider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import static android.app.Activity.RESULT_OK;


public class FPSPickerDialog extends DialogFragment {

  private Button saveButton;
  private Button cancelButton;
  private RadioButton radio23976, radio25, radio2997, radio5994, radio24, radio30, radio50, radio60, radioCustom;
  private EditText customFPS;

  private int fps;

  private int[] predefinedFPS = { 23976, 25000, 29970, 59940, 24000, 30000, 50000, 60000 };

  public FPSPickerDialog() {}

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public static FPSPickerDialog newInstance() {
    FPSPickerDialog dialog = new FPSPickerDialog();

    return dialog;
  }

  public FPSPickerDialog setFPS(int fps) {
    this.fps = fps;
    return this;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_fps_picker, container);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    saveButton = (Button) view.findViewById(R.id.saveButton);
    cancelButton = (Button) view.findViewById(R.id.cancelButton);
    radioCustom = (RadioButton) view.findViewById(R.id.radioCustom);
    radio23976 = (RadioButton) view.findViewById(R.id.radio23976);
    radio25 = (RadioButton) view.findViewById(R.id.radio25);
    radio2997 = (RadioButton) view.findViewById(R.id.radio2997);
    radio5994 = (RadioButton) view.findViewById(R.id.radio5994);
    radio24 = (RadioButton) view.findViewById(R.id.radio24);
    radio30 = (RadioButton) view.findViewById(R.id.radio30);
    radio50 = (RadioButton) view.findViewById(R.id.radio50);
    radio60 = (RadioButton) view.findViewById(R.id.radio60);
    customFPS = (EditText) view.findViewById(R.id.customFPS);

    boolean predefined = false;
    for (int i : predefinedFPS) {
      if (i == fps) {
        predefined = true;
      }
    }

    if (predefined) {
      switch (fps) {
        case 23976:
          radio23976.setChecked(true);
          break;
        case 25000:
          radio25.setChecked(true);
          break;
        case 29970:
          radio2997.setChecked(true);
          break;
        case 59940:
          radio5994.setChecked(true);
          break;
        case 24000:
          radio24.setChecked(true);
          break;
        case 30000:
          radio30.setChecked(true);
          break;
        case 50000:
          radio50.setChecked(true);
          break;
        case 60000:
          radio60.setChecked(true);
          break;
      }
    } else {
      customFPS.setEnabled(true);
      customFPS.setText(Double.toString(fps / 1000.0));
      radioCustom.setChecked(true);
    }

    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent response = new Intent();

        response.putExtra("FPS", fps);

        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, response);
        dismiss();
      }
    });

    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    radioCustom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          customFPS.setEnabled(true);

          if (customFPS.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(customFPS, InputMethodManager.SHOW_IMPLICIT);
          }
        } else {
          customFPS.setEnabled(false);
        }
      }
    });

    customFPS.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
          fps = (int) Double.parseDouble(customFPS.getText().toString()) * 1000;
        } catch (Exception e) {
          fps = 0;
        }
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void afterTextChanged(Editable s) {}
    });

    View.OnClickListener onClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        radio23976.setChecked(false);
        radio25.setChecked(false);
        radio2997.setChecked(false);
        radio5994.setChecked(false);
        radio24.setChecked(false);
        radio30.setChecked(false);
        radio50.setChecked(false);
        radio60.setChecked(false);
        radioCustom.setChecked(false);

        ((RadioButton) v).setChecked(true);

        switch (v.getId()) {
          case R.id.radio23976:
            fps = 23976;
            break;
          case R.id.radio25:
            fps = 25000;
            break;
          case R.id.radio2997:
            fps = 29970;
            break;
          case R.id.radio5994:
            fps = 59940;
            break;
          case R.id.radio24:
            fps = 24000;
            break;
          case R.id.radio30:
            fps = 30000;
            break;
          case R.id.radio50:
            fps = 50000;
            break;
          case R.id.radio60:
            fps = 60000;
            break;
        }
      }
    };

    radio23976.setOnClickListener(onClickListener);
    radio25.setOnClickListener(onClickListener);
    radio2997.setOnClickListener(onClickListener);
    radio5994.setOnClickListener(onClickListener);
    radio24.setOnClickListener(onClickListener);
    radio30.setOnClickListener(onClickListener);
    radio50.setOnClickListener(onClickListener);
    radio60.setOnClickListener(onClickListener);
    radioCustom.setOnClickListener(onClickListener);
  }
}
