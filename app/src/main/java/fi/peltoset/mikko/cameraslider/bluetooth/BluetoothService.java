package fi.peltoset.mikko.cameraslider.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import fi.peltoset.mikko.cameraslider.interfaces.NotificationCommunicatorListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;
import fi.peltoset.mikko.cameraslider.notifications.NotificationCommunicator;

public class BluetoothService extends Service {
  public static final int MESSAGE_CONNECT_TO_DEVICE = 1;
  public static final int MESSAGE_STOP = 2;

  // Broadcast intent codes
  public static final String INTENT_DEVICE_CONNECTED = "INTENT_DEVICE_CONNECTED";
  public static final String INTENT_DEVICE_DISCONNECTED = "INTENT_DEVICE_DISCONNECTED";
  public static final String INTENT_DEVICE_DETECTION_FAILED = "INTENT_DEVICE_DETECTION_FAILED";

  public static final String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";

  private LocalBroadcastManager localBroadcastManager;
  private BluetoothAdapter bluetoothAdapter;
  private BluetoothSocket bluetoothSocket;
  private NotificationCommunicator notificationCommunicator;

  // Handle incoming messages from BluetoothServiceCommunicator
  private class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MESSAGE_CONNECT_TO_DEVICE:
          connectToDevice(msg.getData().getString(EXTRA_DEVICE_ADDRESS));
          break;
        case MESSAGE_STOP:
          stopAndCancelNotification();
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }

  private final Messenger messenger = new Messenger(new IncomingHandler());

  // Called when the service is started with startService
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    notificationCommunicator = new NotificationCommunicator(this, new NotificationCommunicatorListener() {
      @Override
      public void onNotificationStartPauseButtonPressed() {
        Log.d(Constants.TAG, "pauseplay");
      }

      @Override
      public void onNotificationStopButtonPressed() {
        Log.d(Constants.TAG, "stop");
      }
    });

    notificationCommunicator.displaySampleNotification();

    return super.onStartCommand(intent, flags, startId);
  }

  // Called when an Activity calls bindService. Returns the Messenger used to send data back to
  // the service.
  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

    return messenger.getBinder();
  }

  /**
   * Try initializing a connection to a Bluetooth device
   *
   * @param address
   */
  private void connectToDevice(String address) {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    bluetoothAdapter.cancelDiscovery();

    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

    ConnectThread connectThread = new ConnectThread(device, new ConnectThread.OnConnectListener() {
      @Override
      public void onConnect(BluetoothSocket socket) {
        bluetoothSocket = socket;
        BluetoothService.this.onConnect();
      }
    });

    connectThread.start();
  }

  /**
   * Cancel all notifications and stop the background service
   */
  private void stopAndCancelNotification() {
    notificationCommunicator.cancel();
    stopSelf();
  }

  private void onConnect() {
    ConnectedThread connectedThread = new ConnectedThread(bluetoothSocket, new ConnectedThread.SocketListener() {
      @Override
      public void onConnect() {
        Log.i(Constants.TAG, "device connected");
        localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_CONNECTED));
      }

      @Override
      public void onDisconnect() {
        Log.i(Constants.TAG, "device disconnected");
      }

      @Override
      public void onDeviceDetectionFail() {
        Log.i(Constants.TAG, "detection failed");
      }

      @Override
      public void onNewMessage(String message) {
        Log.i(Constants.TAG, "message: " + message);
      }
    });

    connectedThread.start();
  }
}
