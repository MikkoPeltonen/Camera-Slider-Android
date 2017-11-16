package fi.peltoset.mikko.cameraslider.eventbus;

import fi.peltoset.mikko.cameraslider.miscellaneous.Axis;
import fi.peltoset.mikko.cameraslider.miscellaneous.RotationDirection;

public class ManualMoveButtonHoldEvent {
  private Axis axis;
  private RotationDirection direction;

  public ManualMoveButtonHoldEvent(Axis axis, RotationDirection direction) {
    this.axis = axis;
    this.direction = direction;
  }

  public Axis getAxis() {
    return this.axis;
  }

  public RotationDirection getDirection() {
    return this.direction;
  }
}
