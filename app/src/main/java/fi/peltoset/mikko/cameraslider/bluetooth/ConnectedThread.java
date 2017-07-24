package fi.peltoset.mikko.cameraslider.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ConnectedThread extends Thread {
  private BluetoothSocket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  private SocketListener listener;

  interface SocketListener {
    void onConnect();
    void onDisconnect();
    void onDeviceDetectionFail();
    void onNewMessage(String message);
  }

  public ConnectedThread(BluetoothSocket socket, final SocketListener listener) {
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

    write("Hello, Camera Slider!");
    try {
      initialLine = bufferedReader.readLine();
    } catch (IOException e) {
      listener.onDisconnect();
      e.printStackTrace();
    }

    if (initialLine == null || !initialLine.equals("Hello, Android!")) {
      listener.onDeviceDetectionFail();
      cancel();
    } else {
      listener.onConnect();
    }
  }

  /**
   * Send a string to the Bluetooth device
   *
   * @param message
   */
  public void write(String message) {
    try {
      bufferedWriter.write(message + "\n");
      bufferedWriter.flush();
    } catch (IOException e) {
      listener.onDisconnect();
      cancel();
      e.printStackTrace();
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
      cancel();
      e.printStackTrace();
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
  }
}