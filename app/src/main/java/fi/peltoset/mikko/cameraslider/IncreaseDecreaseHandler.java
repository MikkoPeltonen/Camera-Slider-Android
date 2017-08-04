package fi.peltoset.mikko.cameraslider;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import fi.peltoset.mikko.cameraslider.interfaces.IncreaseDecreaseListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.RotationDirection;

public class IncreaseDecreaseHandler {

  private IncreaseDecreaseListener listener;
  private View increaseButton, decreaseButton;

  private Handler handler = new Handler();

  private RepeatRunnable repeatRunnable = new RepeatRunnable();

  private enum State {
    INCREASE, DECREASE, NONE
  }

  private State currentState = State.NONE;

  // When long pressed this defines how often a increase/decrease action is fired
  private static final int DELAY = 25;

  private boolean isLongPress = false;

  private Runnable detectLongPress = new Runnable() {
    @Override
    public void run() {
      if (currentState == State.INCREASE) {
        IncreaseDecreaseHandler.this.listener.onIncreaseButtonStateChange(true);
      } else if (currentState == State.DECREASE) {
        IncreaseDecreaseHandler.this.listener.onDecreaseButtonStateChange(true);
      }

      isLongPress = true;

      handler.post(new RepeatRunnable());
    }
  };

  public IncreaseDecreaseHandler(View increaseButton, View decreaseButton, IncreaseDecreaseListener increaseDecreaseListener) {
    this.listener = increaseDecreaseListener;
    this.increaseButton = increaseButton;
    this.decreaseButton = decreaseButton;

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          // Button pressed

          if (v.getId() == IncreaseDecreaseHandler.this.increaseButton.getId()) {
            currentState = State.INCREASE;
            listener.step(RotationDirection.CW);
            listener.onIncrease();
          } else if (v.getId() == IncreaseDecreaseHandler.this.decreaseButton.getId()) {
            currentState = State.DECREASE;
            listener.step(RotationDirection.CCW);
            listener.onDecrease();
          }

          handler.postDelayed(detectLongPress, 500);
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
          // Button released

          handler.removeCallbacks(detectLongPress);

          if (isLongPress) {
            if (currentState == State.INCREASE) {
              IncreaseDecreaseHandler.this.listener.onIncreaseButtonStateChange(false);
            } else if (currentState == State.DECREASE) {
              IncreaseDecreaseHandler.this.listener.onDecreaseButtonStateChange(false);
            }
          }

          isLongPress = false;
          currentState = State.NONE;
        }

        return true;
      }
    };

    increaseButton.setOnTouchListener(onTouchListener);
    decreaseButton.setOnTouchListener(onTouchListener);
  }

  private class RepeatRunnable implements Runnable {
    @Override
    public void run() {
      // Depending on the current state we inform the listener about what to do and if the button
      // is still pressed, fire the action again after DELAY.
      if (currentState == State.INCREASE) {
        IncreaseDecreaseHandler.this.listener.onIncrease();
      } else if (currentState == State.DECREASE) {
        IncreaseDecreaseHandler.this.listener.onDecrease();
      }

      if (currentState != State.NONE) {
        handler.postDelayed(new RepeatRunnable(), DELAY);
      }
    }
  }
}
