package fi.peltoset.mikko.cameraslider.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

public class BluetoothService extends Service {
  private ConnectedThread connectedThread;
  private Messenger listener;

  public static final int MSG_CONNECTED = 1;
  public static final int MSG_DISCONNECTED = 2;
  public static final int MSG_VERIFICATION_FAIL = 3;
  public static final int MSG_MESSAGE = 4;

  public static final String MSG_MESSAGE_DATA_MESSAGE = "MESSAGE";

  public class LocalBinder extends Binder {
    BluetoothService getService() {
      return BluetoothService.this;
    }
  }

  // Called when the service is started with startService
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  // Called when an Activity calls bindService. Returns the Messenger used to send data back to
  // the service.
  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new LocalBinder();
  }

  // Handle ConnecThread messages.
  private ConnectThread.ConnectThreadListener connectThreadListener = new ConnectThread.ConnectThreadListener() {
    @Override
    public void onConnect(BluetoothSocket socket) {
      // ConnectThread successfully opened a connection to a Bluetooth device. Using the acquired
      // socket we open a new thread, ConnectedThread, to handle the socket communications, e.g.
      // listening to incoming messages and sending out messages. However, before being able to
      // receive or send messages, the remote device must be verified to be a Camera Slider.
      // ConnectThread can open a connection to any Bluetooth device and therefore the
      // ConnectedThread must communicate with the remote device to detect it's purpose. This is
      // done by sending a specific handshake message and if a correct answer is received, the
      // device is accepted. Otherwise the connection is canceled.
      connectedThread = new ConnectedThread(socket, connectedThreadListener);
      connectedThread.start();
    }

    @Override
    public void onConnectionFail() {
      // Opening the socket failed.
      sendMessage(MSG_DISCONNECTED, null);
    }
  };

  // Handle ConnectedThread messages.
  private ConnectedThread.ConnectedThreadListener connectedThreadListener = new ConnectedThread.ConnectedThreadListener() {
    @Override
    public void onConnect() {
      sendMessage(MSG_CONNECTED, null);
    }

    @Override
    public void onDisconnect() {
      // The Bluetooth connection was closed.
      sendMessage(MSG_DISCONNECTED, null);
    }

    @Override
    public void onVerificationFail() {
      sendMessage(MSG_VERIFICATION_FAIL, null);
    }

    @Override
    public void onNewMessage(byte[] message) {
      Bundle data = new Bundle();
      data.putByteArray(MSG_MESSAGE_DATA_MESSAGE, message);
      sendMessage(MSG_MESSAGE, data);
    }
  };

  /**
   * Send a message to CameraSliderCommunicator
   *
   * @param message
   */
  private void sendMessage(int message, Bundle data) {
    if (listener == null) {
      throw new RuntimeException("Listener can't be null");
    }

    Message msg = Message.obtain(null, message);
    msg.setData(data);

    try {
      listener.send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  /**
   * Add a Messenger to listen to messages sent from the BluetoothService.
   *
   * @param listener
   */
  public void addListener(Messenger listener) {
    this.listener = listener;
  }

  /**
   * Try initializing a connection to a Bluetooth device
   *
   * @param address
   */
  public void connectToDevice(String address) {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);

    // Asynchronously connect to the Bluetooth device. ConnectThread tries opening a socket to the
    // given Bluetooth device.
    ConnectThread connectThread = new ConnectThread(device, connectThreadListener);
    connectThread.start();
  }

  /**
   * Cancel all notifications and stop the background service
   */
  public void stopAndCancelNotification() {
    if (connectedThread != null) {
      connectedThread.cancel();
    }

    stopSelf();
  }

  /**
   * Send a command to the Camera Slider.
   *
   * @param command Command byte
   * @param payload Payload
   */
  public void sendCommand(byte command, byte[] payload) {
    connectedThread.write(command, payload);
  }
}
