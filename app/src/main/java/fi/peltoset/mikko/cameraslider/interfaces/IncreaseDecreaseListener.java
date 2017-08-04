package fi.peltoset.mikko.cameraslider.interfaces;

import fi.peltoset.mikko.cameraslider.miscellaneous.RotationDirection;

public interface IncreaseDecreaseListener {
  void onIncrease();
  void onDecrease();
  void onIncreaseButtonStateChange(boolean pressed);
  void onDecreaseButtonStateChange(boolean pressed);
  void step(RotationDirection rotationDirection);
}
