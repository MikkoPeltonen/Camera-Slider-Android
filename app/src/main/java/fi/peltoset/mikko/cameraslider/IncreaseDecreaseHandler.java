package fi.peltoset.mikko.cameraslider;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class IncreaseDecreaseHandler {

  private IncreaseDecreaseListener listener;
  private View increaseButton, decreaseButton;

  private Handler handler = new Handler();

  private View.OnClickListener onClickListener;
  private View.OnLongClickListener onLongClickListener;
  private View.OnTouchListener onTouchListener;

  private enum State {
    INCREASE, DECREASE, NONE
  }

  private State currentState = State.NONE;

  // When long pressed this defines how often a increase/decrease action is fired
  private static final int DELAY = 25;


  public IncreaseDecreaseHandler(View increaseButton, View decreaseButton, IncreaseDecreaseListener listener) {
    this.listener = listener;
    this.increaseButton = increaseButton;
    this.decreaseButton = decreaseButton;

    // Handle single increase and decrease button clicks
    onClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (v.getId() == IncreaseDecreaseHandler.this.increaseButton.getId()) {
          IncreaseDecreaseHandler.this.listener.onIncrease();
        } else if (v.getId() == IncreaseDecreaseHandler.this.decreaseButton.getId()) {
          IncreaseDecreaseHandler.this.listener.onDecrease();
        }
      }
    };

    // Handle long clicks by posting the Handler with the RepeatRunnable
    onLongClickListener = new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (v.getId() == IncreaseDecreaseHandler.this.increaseButton.getId()) {
          currentState = State.INCREASE;
        } else if (v.getId() == IncreaseDecreaseHandler.this.decreaseButton.getId()) {
          currentState = State.DECREASE;
        }

        handler.post(new RepeatRunnable());

        return false;
      }
    };

    // When either of the buttons is released, set current state to none
    onTouchListener = new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)) {
          currentState = State.NONE;
        }

        return false;
      }
    };

    this.increaseButton.setOnClickListener(onClickListener);
    this.increaseButton.setOnLongClickListener(onLongClickListener);
    this.increaseButton.setOnTouchListener(onTouchListener);

    this.decreaseButton.setOnClickListener(onClickListener);
    this.decreaseButton.setOnLongClickListener(onLongClickListener);
    this.decreaseButton.setOnTouchListener(onTouchListener);
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
