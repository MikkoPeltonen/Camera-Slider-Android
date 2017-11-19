package fi.peltoset.mikko.cameraslider.interfaces;

public interface IncreaseDecreaseListener {
  void onIncrease();
  void onDecrease();
  void onIncreaseButtonStateChange(boolean pressed);
  void onDecreaseButtonStateChange(boolean pressed);
  void onStop();
}
