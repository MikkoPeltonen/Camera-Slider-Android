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

  /**
   * Convert a 32 bit integer to byte array, MSB first.
   *
   * @param n Number to convert
   * @return 4-byte array
   */
  public static byte[] intToByteArray(int n) {
    byte[] bytes = new byte[4];

    bytes[0] = (byte) ((n >> 24) & 0xFF);
    bytes[1] = (byte) ((n >> 16) & 0xFF);
    bytes[2] = (byte) ((n >> 8) & 0xFF);
    bytes[3] = (byte) (n & 0xFF);

    return bytes;
  }

  /**
   * Convert a 4-byte array representing a 32 bit integer into int. MSB first.
   *
   * @param bytes
   * @return
   */
  public static int byteArrayToInt(byte[] bytes) {
    return bytes[0] << 24 | bytes[1] << 16 | bytes[2] << 8 | bytes[3];
  }
}
