package fi.peltoset.mikko.cameraslider.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import fi.peltoset.mikko.cameraslider.interfaces.NotificationCommunicatorListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;
import fi.peltoset.mikko.cameraslider.notifications.NotificationCommunicator;

public class BluetoothService extends Service {
  public static final int MESSAGE_CONNECT_TO_DEVICE = 1;
  public static final int MESSAGE_STOP = 2;
  public static final int MESSAGE_START_ACTION = 3;

  // Broadcast intent codes
  public static final String INTENT_SERVICE_STARTED = "INTENT_SERVICE_STARTED";
  public static final String INTENT_DEVICE_CONNECTED = "INTENT_DEVICE_CONNECTED";
  public static final String INTENT_DEVICE_DISCONNECTED = "INTENT_DEVICE_DISCONNECTED";
  public static final String INTENT_DEVICE_DETECTION_FAILED = "INTENT_DEVICE_DETECTION_FAILED";
  public static final String INTENT_ACTION_STARTED = "INTENT_ACTION_STARTED";
  public static final String INTENT_ACTION_PAUSED = "INTENT_ACTION_PAUSED";
  public static final String INTENT_ACTION_RESUMED = "INTENT_ACTION_RESUMED";
  public static final String INTENT_ACTION_STOPPED = "INTENT_ACTION_STOPPED";
  public static final String INTENT_STATUS_DEVICE_CONNECTED = "INTENT_STATUS_DEVICE_CONNECTED";
  public static final String INTENT_STATUS_DEVICE_NOT_CONNECTED = "INTENT_STATUS_DEVICE_NOT_CONNECTED";
  public static final String INTENT_DEVICE_CONNECTION_FAILED = "INTENT_DEVICE_CONNECTION_FAILED";

  public static final String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
  public static final String EXTRA_ACTION_TYPE = "EXTRA_ACTION_TYPE";
  public static final String EXTRA_TIMELAPSE_INTERVAL = "EXTRA_TIMELAPSE_INTERVAL";
  public static final String EXTRA_TIMELAPSE_FPS = "EXTRA_TIMELAPSE_FPS";
  public static final String EXTRA_TIMELAPSE_VIDEO_KEYFRAMES = "EXTRA_TIMELAPSE_VIDEO_KEYFRAMES";

  private LocalBroadcastManager localBroadcastManager;
  private BluetoothAdapter bluetoothAdapter;
  private BluetoothSocket bluetoothSocket;
  private NotificationCommunicator notificationCommunicator;
  private CameraSliderCommunicatorThread cameraSlider;

  private String connectedDeviceAddress = null;

  private boolean isDeviceConnected = false;
  private boolean isActionRunning = false;

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
        case MESSAGE_START_ACTION:
          startAction(msg.getData());
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
    localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

    localBroadcastManager.sendBroadcast(new Intent(INTENT_SERVICE_STARTED));

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

    // To keep the service running indefinitely we must start a sticky notification
    notificationCommunicator.displayInfoNotification("Camera Slider", "Not connected");

    return START_STICKY;
  }

  // Called when an Activity calls bindService. Returns the Messenger used to send data back to
  // the service.
  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    // When an activity binds to the service and if a device is already connected, notify the
    // listener
    if (isDeviceConnected) {
      localBroadcastManager.sendBroadcast(new Intent(INTENT_STATUS_DEVICE_CONNECTED));
    }

    return messenger.getBinder();
  }

  /**
   * Try initializing a connection to a Bluetooth device
   *
   * @param address
   */
  private void connectToDevice(final String address) {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    bluetoothAdapter.cancelDiscovery();

    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

    ConnectThread connectThread = new ConnectThread(device, new ConnectThread.OnConnectListener() {
      @Override
      public void onConnect(BluetoothSocket socket) {
        bluetoothSocket = socket;
        connectedDeviceAddress = address;
        onDeviceConnected();
      }
    });

    connectThread.start();
  }

  /**
   * Receives an action, parses it and sends the data to the connected device
   *
   * @param data
   */
  private void startAction(Bundle data) {
    String actionType = data.getString(EXTRA_ACTION_TYPE);

    cameraSlider.write("BEGIN_TRANSACTION");
    cameraSlider.write("ACTION_TYPE:" + actionType);

    if (actionType.equals(Constants.ACTION_TIMELAPSE)) {
      int interval = data.getInt(EXTRA_TIMELAPSE_INTERVAL);
      int fps = data.getInt(EXTRA_TIMELAPSE_FPS);

      cameraSlider.write("INTERVAL:" + interval);
      cameraSlider.write("FPS:" + fps);
    } else if (actionType.equals(Constants.ACTION_VIDEO)) {

    } else if (actionType.equals(Constants.ACTION_PANORAMA)) {

    } else if (actionType.equals(Constants.ACTION_MANUAL)) {

    }

    cameraSlider.write("END_TRANSACTION");
  }

  /**
   * Cancel all notifications and stop the background service
   */
  private void stopAndCancelNotification() {
    notificationCommunicator.cancel();

    if (cameraSlider != null) {
      cameraSlider.cancel();
    }

    stopSelf();
  }

  /**
   * Handle incoming messages coming from the Camera Slider
   *
   * @param message
   */
  private void handleCameraSliderMessages(String message) {
    // Split message into parts. The structure is as follows:
    // key1:value1;key2:value2;key3:value3
    Map<String, String> parameters = new HashMap<>();

    String[] keyValuePairs = message.split(";");
    for (int i = 0; i < keyValuePairs.length; i += 1) {
      String[] splitKeyValuePairs = keyValuePairs[i].split(":");
      parameters.put(splitKeyValuePairs[0], splitKeyValuePairs[1]);
    }

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      Log.d(Constants.TAG, "entry: " + entry.getKey() + " " + entry.getValue());
    }

    if (message.startsWith("STATUS:")) {
      Log.d(Constants.TAG, "status: " + message);
    }
  }

  private void onDeviceConnected() {
    cameraSlider = new CameraSliderCommunicatorThread(bluetoothSocket, new CameraSliderCommunicatorThread.SocketListener() {
      @Override
      public void onConnect() {
        Log.i(Constants.TAG, "device connected");
        notificationCommunicator.displayInfoNotification("Camera Slider", "Connected");
        localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_CONNECTED));
        isDeviceConnected = true;
      }

      @Override
      public void onDisconnect() {
        if (isDeviceConnected) {
          Log.i(Constants.TAG, "device disconnected");
          notificationCommunicator.displayInfoNotification("Camera Slider", "Not connected");
          localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_DISCONNECTED));
        }

        isDeviceConnected = false;
      }

      @Override
      public void onConnectionFailed() {
        notificationCommunicator.displayInfoNotification("Camera Slider", "Not connected");
        localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_CONNECTION_FAILED));
      }

      @Override
      public void onDeviceDetectionFail() {
        Log.i(Constants.TAG, "detection failed");
        notificationCommunicator.displayInfoNotification("Camera Slider", "Not connected");
        localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_DETECTION_FAILED));
      }

      @Override
      public void onNewMessage(String message) {
        handleCameraSliderMessages(message);
      }
    });

    cameraSlider.start();
  }
}
