package fi.peltoset.mikko.cameraslider;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 *
 */
public class KeyframePOJO {
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

  public String getFormattedDuration() {
    return (int) (this.duration / 60) + " min " + (int) (this.duration % 60) + " s";
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
    return formatAngle(this.panAngle) + "°";
  }

  public void setPanAngle(int panAngle) {
    this.panAngle = panAngle;
  }

  public int getTiltAngle() {
    return this.tiltAngle;
  }

  public String getFormattedTiltAngle() {
    return formatAngle(this.tiltAngle) + "°";
  }

  public void setTiltAngle(int tiltAngle) {
    this.tiltAngle = tiltAngle;
  }

  public int getZoom() {
    return this.zoom;
  }

  public String getFormattedZoom() {
    return formatAngle(this.zoom) + "°";
  }

  public void setZoom(int zoom) {
    this.zoom = zoom;
  }

  public int getFocus() {
    return this.focus;
  }

  public String getFormattedFocus() {
    return formatAngle(this.focus) + "°";
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
}
