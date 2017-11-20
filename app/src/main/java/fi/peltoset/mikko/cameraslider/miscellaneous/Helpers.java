package fi.peltoset.mikko.cameraslider.miscellaneous;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;

public class Helpers {
  public static String formatAngle(int angle) {
    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    return df.format(angle / 100.0);
  }

  /**
   * Strip start, command and stop bytes from the message.
   *
   * @param message Raw message
   * @return Payload
   */
  public static byte[] getPayload(byte[] message) {
    return Arrays.copyOfRange(message, 2, message.length - 1);
  }
}
