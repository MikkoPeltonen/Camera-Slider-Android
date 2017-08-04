package fi.peltoset.mikko.cameraslider;

import fi.peltoset.mikko.cameraslider.interfaces.IncreaseDecreaseListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.RotationDirection;

public class IncreaseDecreaseHandlerStub implements IncreaseDecreaseListener {
  @Override
  public void onIncrease() {}

  @Override
  public void onDecrease() {}

  @Override
  public void onIncreaseButtonStateChange(boolean pressed) {}

  @Override
  public void onDecreaseButtonStateChange(boolean pressed) {}

  @Override
  public void step(RotationDirection rotationDirection) {}
}
