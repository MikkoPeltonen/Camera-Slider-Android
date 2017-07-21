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

  public ConnectThread(BluetoothDevice device, OnConnectListener listener) {
    this.listener = listener;

    BluetoothSocket tmpSocket = null;

    try {
      Method method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
      tmpSocket = (BluetoothSocket) method.invoke(device, 1);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    socket = tmpSocket;
  }

  /**
   * Connect or fail
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
