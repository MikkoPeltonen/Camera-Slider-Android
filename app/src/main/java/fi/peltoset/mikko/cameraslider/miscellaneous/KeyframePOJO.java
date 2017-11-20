package fi.peltoset.mikko.cameraslider.miscellaneous;

import android.animation.Keyframe;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import fi.peltoset.mikko.cameraslider.bluetooth.ConnectionConstants;

/**
 *
 */
public class KeyframePOJO implements Parcelable {
  private int duration = 0; // milliseconds
  private int slideLength = 0; // mm
  private int panAngle = 0; // 1/100 of a degree
  private int tiltAngle = 0; // 1/100 of a degree
  private int zoom = 0; // 1/100 of a degree
  private int focus = 0; // 1/100 of a degree

  public KeyframePOJO() {}

  public KeyframePOJO(int duration, int slideLength, int panAngle, int tiltAngle, int zoom, int focus) {
    this.duration = duration;
    this.slideLength = slideLength;
    this.panAngle = panAngle;
    this.tiltAngle = tiltAngle;
    this.zoom = zoom;
    this.focus = focus;
  }


  public int getDuration() {
    return this.duration;
  }

  public String getFormattedDuration(int fps, int interval) {
    double waitTime = (this.duration / 1000.0) * (fps / 1000.0) * (interval / 1000.0);

    if (waitTime >= 3600) {
      return (int) (waitTime / 3600) + " h " + (int) (waitTime % 3600 / 3600 * 60) + " min";
    }

    return (int) (waitTime / 60) + " min " + (int) (waitTime % 60) + " s";
  }

  public String getFormattedVideoLength() {
    DecimalFormat df = new DecimalFormat("0.0");
    df.setRoundingMode(RoundingMode.HALF_UP);
    return df.format(duration / 1000.0) + " s";
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public int getSlideLength() {
    return this.slideLength;
  }

  public String getFormattedSlideLength() {
    return this.slideLength + " mm";
  }

  public void setSlideLength(int slideLength) {
    this.slideLength = slideLength;
  }

  public int getPanAngle() {
    return this.panAngle;
  }

  public String getFormattedPanAngle() {
    return formatAngle(this.panAngle) + " °";
  }

  public void setPanAngle(int panAngle) {
    this.panAngle = panAngle;
  }

  public int getTiltAngle() {
    return this.tiltAngle;
  }

  public String getFormattedTiltAngle() {
    return formatAngle(this.tiltAngle) + " °";
  }

  public void setTiltAngle(int tiltAngle) {
    this.tiltAngle = tiltAngle;
  }

  public int getZoom() {
    return this.zoom;
  }

  public String getFormattedZoom() {
    return formatAngle(this.zoom) + " °";
  }

  public void setZoom(int zoom) {
    this.zoom = zoom;
  }

  public int getFocus() {
    return this.focus;
  }

  public String getFormattedFocus() {
    return formatAngle(this.focus) + " °";
  }

  public void setFocus(int focus) {
    this.focus = focus;
  }

  /**
   *
   * @param angle
   * @return
   */
  private String formatAngle(int angle) {
    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);
    return df.format(angle / 100.0);
  }

  // Parcelable stuff

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(duration);
    out.writeInt(slideLength);
    out.writeInt(panAngle);
    out.writeInt(tiltAngle);
    out.writeInt(zoom);
    out.writeInt(focus);
  }

  public static final Parcelable.Creator<KeyframePOJO> CREATOR = new Parcelable.Creator<KeyframePOJO>() {
    @Override
    public KeyframePOJO createFromParcel(Parcel source) {
      return new KeyframePOJO(source);
    }

    @Override
    public KeyframePOJO[] newArray(int size) {
      return new KeyframePOJO[size];
    }
  };

  private KeyframePOJO(Parcel in) {
    duration = in.readInt();
    slideLength = in.readInt();
    panAngle = in.readInt();
    tiltAngle = in.readInt();
    zoom = in.readInt();
    focus = in.readInt();
  }

  /**
   * Create a KeyframePOJO from raw bytes. This is used when connecting to a Camera Slider that is
   * running an action and sends its data back to the client.
   *
   * @param bytes
   */
  public KeyframePOJO(byte[] bytes) {
    // TODO
  }


  /**
   * Convert KeyframePOJO to a byte array that can be send to the Camera Slider.
   *
   * @return Byte array representation of the Keyframe
   */
  public byte[] toByteArray() {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    buffer.write(Helpers.intToByteArray(duration), 0, 4);
    buffer.write(Helpers.intToByteArray(slideLength), 0, 4);
    buffer.write(Helpers.intToByteArray(panAngle), 0, 4);
    buffer.write(Helpers.intToByteArray(tiltAngle), 0, 4);
    buffer.write(Helpers.intToByteArray(focus), 0, 4);
    buffer.write(Helpers.intToByteArray(zoom), 0, 4);

    return buffer.toByteArray();
  }
}
