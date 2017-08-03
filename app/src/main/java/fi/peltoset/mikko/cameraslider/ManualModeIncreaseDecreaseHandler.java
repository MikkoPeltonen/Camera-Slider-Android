package fi.peltoset.mikko.cameraslider;

import android.view.MotionEvent;
import android.view.View;

import fi.peltoset.mikko.cameraslider.interfaces.IncreaseDecreaseListener;

public class ManualModeIncreaseDecreaseHandler extends IncreaseDecreaseHandler {
  public interface ManualModeIncreaseDecreaseListener extends IncreaseDecreaseListener {
    void onIncreaseButtonStateChange(boolean pressed);
    void onDecreaseButtonStateChange(boolean pressed);
  }

  public ManualModeIncreaseDecreaseHandler(View increaseButton, View decreaseButton, final ManualModeIncreaseDecreaseListener listener) {
    super(increaseButton, decreaseButton, listener);

    increaseButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          listener.onIncreaseButtonStateChange(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
          listener.onIncreaseButtonStateChange(false);
        }

        return false;
      }
    });

    decreaseButton.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          listener.onDecreaseButtonStateChange(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
          listener.onDecreaseButtonStateChange(false);
        }

        return false;
      }
    });
  }
}
