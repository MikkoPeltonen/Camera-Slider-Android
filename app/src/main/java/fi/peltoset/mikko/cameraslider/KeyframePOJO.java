package fi.peltoset.mikko.cameraslider;

/**
 *
 */
public class KeyframePOJO {
  private int duration;
  private double slideLength;
  private double panAngle;
  private double tiltAngle;
  private double zoom;
  private double focus;

  public KeyframePOJO() {
  }

  public KeyframePOJO(int duration, double slideLength, double panAngle, double tiltAngle, double zoom, double focus) {
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

  public double getSlideLength() {
    return this.slideLength;
  }

  public String getFormattedSlideLength() {
    return this.slideLength + " mm";
  }

  public void setSlideLength(int slideLength) {
    this.slideLength = slideLength;
  }

  public double getPanAngle() {
    return this.panAngle;
  }

  public String getFormattedPanAngle() {
    return Math.abs(this.panAngle) + "° " + (this.panAngle < 0 ? "CCW" : "CW");
  }

  public void setPanAngle(double panAngle) {
    this.panAngle = panAngle;
  }

  public double getTiltAngle() {
    return this.tiltAngle;
  }

  public String getFormattedTiltAngle() {
    return Math.abs(this.tiltAngle) + "° " + (this.tiltAngle < 0 ? "CCW" : "CW");
  }

  public void setTiltAngle(double tiltAngle) {
    this.tiltAngle = tiltAngle;
  }

  public double getZoom() {
    return this.zoom;
  }

  public String getFormattedZoom() {
    return Math.abs(this.zoom) + "° " + (this.zoom < 0 ? "CCW" : "CW");
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  public double getFocus() {
    return this.focus;
  }

  public String getFormattedFocus() {
    return Math.abs(this.focus) + "° " + (this.focus < 0 ? "CCW" : "CW");
  }

  public void setFocus(double focus) {
    this.focus = focus;
  }
}
