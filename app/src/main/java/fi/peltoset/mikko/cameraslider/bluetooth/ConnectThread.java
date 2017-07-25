package fi.peltoset.mikko.cameraslider.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConnectThread extends Thread {
  private final BluetoothSocket socket;
  private OnConnectListener listener;

  interface OnConnectListener {
    void onConnect(BluetoothSocket socket);
  }

  /**
   * Try to connect to a Bluetooth device.
   *
   * @param device
   * @param listener
   */
  public ConnectThread(BluetoothDevice device, OnConnectListener listener) {
    this.listener = listener;

    BluetoothSocket tmpSocket = null;

    try {
      Method method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
      tmpSocket = (BluetoothSocket) method.invoke(device, 1);
    } catch (Exception e) {
      e.printStackTrace();
    }

    socket = tmpSocket;
  }

  /**
   * This will either connect or fail and in either case will run straight through.
   */
  public void run() {
    try {
      socket.connect();
    } catch (IOException e) {
      e.printStackTrace();

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
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
