package fi.peltoset.mikko.cameraslider.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CameraSliderCommunicatorThread extends Thread {
  private BluetoothSocket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  private SocketListener listener;

  interface SocketListener {
    void onConnect(String deviceAddress);
    void onDisconnect();
    void onConnectionFailed();
    void onDeviceDetectionFail();
    void onNewMessage(String message);
  }

  public CameraSliderCommunicatorThread(BluetoothSocket socket, final SocketListener listener) {
    this.socket = socket;
    this.listener = listener;

    try {
      bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Test that the device is a true Camera Slider. Send a line, expect the device to respond
    // with a correct string.
    String initialLine = null;

    byte[] handshakeMessage = ConnectionConstants.HANDSHAKE_RESPONSE.getBytes();
    byte[] msg = new byte[handshakeMessage.length + 3];
    msg[0] = ConnectionConstants.FLAG_START;
    msg[1] = ConnectionConstants.SEND_HANDSHAKE_GREETING;
    System.arraycopy(handshakeMessage, 0, msg, 1, handshakeMessage.length);
    msg[handshakeMessage.length + 2] = ConnectionConstants.FLAG_STOP;

    write(msg);

    try {
      initialLine = bufferedReader.readLine();
    } catch (IOException e) {
      listener.onConnectionFailed();
      e.printStackTrace();
      cancel();
    }

    if (initialLine == null || !initialLine.equals("Hello, Android!")) {
      listener.onDeviceDetectionFail();
      cancel();
    } else {
      listener.onConnect(socket.getRemoteDevice().getAddress());

      // Request device status on initial connection
    }
  }

  public void write(byte[] message) {
    char[] buf = new char[message.length];
    for (int i = 0; i < message.length; i += 1) {
      buf[i] = (char) (message[i] & 0xff);
    }

    try {
      bufferedWriter.write(buf);
      bufferedWriter.flush();
    } catch (IOException e) {
      listener.onDisconnect();
      e.printStackTrace();
      cancel();
    }
  }

  /**
   * Start listening to incoming lines
   */
  public void run() {
    String line;
    try {
      while ((line = bufferedReader.readLine()) != null) {
        listener.onNewMessage(line);
      }
    } catch (IOException e) {
      listener.onDisconnect();
      e.printStackTrace();
      cancel();
    }
  }

  /**
   * Closes the socket and terminates the thread
   */
  public void cancel() {
    try {
      bufferedReader.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    Thread.currentThread().interrupt();

  }
}
