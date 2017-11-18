package fi.peltoset.mikko.cameraslider.eventbus;

public class SendNewCommandEvent {
  private byte command;
  private byte[] payload;

  public SendNewCommandEvent(byte command, byte[] payload) {
    this.command = command;
    this.payload = payload;
  }

  public byte getCommand() {
    return command;
  }

  public byte[] getPayload() {
    return payload;
  }
}
