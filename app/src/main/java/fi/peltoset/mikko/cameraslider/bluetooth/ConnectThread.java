package fi.peltoset.mikko.cameraslider.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
  private final BluetoothSocket socket;
  private ConnectThreadListener listener;

  interface ConnectThreadListener {
    void onConnect(BluetoothSocket socket);
    void onConnectionFail();
  }

  /**
   * Try to connect to a Bluetooth device.
   *
   * @param device
   * @param listener
   */
  public ConnectThread(BluetoothDevice device, ConnectThreadListener listener) {
    this.listener = listener;

    BluetoothSocket tmpSocket = null;

    try {
      tmpSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
    } catch (Exception e) {
      e.printStackTrace();
      listener.onConnectionFail();
      cancel();
    }

    socket = tmpSocket;
  }

  /**
   * This will either connect or fail and in either case will run straight through.
   */
  public void run() {
    if (socket == null) {
      cancel();
    }

    try {
      socket.connect();
    } catch (IOException e) {
      e.printStackTrace();
      listener.onConnectionFail();

      try {
        socket.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }

    // Inform the listener about a succesful connection.
    listener.onConnect(socket);
  }

  public void cancel() {
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    Thread.currentThread().interrupt();
  }
}
