package fi.peltoset.mikko.cameraslider.miscellaneous;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Helpers {
  public static String formatAngle(int angle) {
    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    return df.format(angle / 100.0);
  }
}
