package fi.peltoset.mikko.cameraslider.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.eventbus.CameraSliderConnectedEvent;
import fi.peltoset.mikko.cameraslider.eventbus.CameraSliderDisconnectedEvent;
import fi.peltoset.mikko.cameraslider.eventbus.ManualMoveButtonHoldEvent;
import fi.peltoset.mikko.cameraslider.interfaces.NotificationCommunicatorListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;
import fi.peltoset.mikko.cameraslider.miscellaneous.Motor;
import fi.peltoset.mikko.cameraslider.miscellaneous.RotationDirection;
import fi.peltoset.mikko.cameraslider.notifications.NotificationCommunicator;

public class BluetoothService extends Service {
  public static final int MESSAGE_CONNECT_TO_DEVICE = 1;
  public static final int MESSAGE_STOP = 2;
  public static final int MESSAGE_START_ACTION = 3;
  public static final int MESSAGE_STEP = 4;
  public static final int MESSAGE_MOVE = 5;

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
  private NotificationCommunicator notificationCommunicator;
  private CameraSliderCommunicatorThread cameraSlider;
  private EventBus eventBus;

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
          break;
        case MESSAGE_STEP:
          break;
        case MESSAGE_MOVE:
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

    eventBus = EventBus.getDefault();

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

    // To keep the service running indefinitely we must start a sticky foreground notification
    notificationCommunicator.displayInfoNotification(R.string.notification_not_connected);

    // BroadcastReceiver for receiving actions from notifications
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(NotificationCommunicator.INTENT_RECONNECT);
    registerReceiver(notificationBroadcastReceiver, intentFilter);

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    unregisterReceiver(notificationBroadcastReceiver);
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
   * Cancel all notifications and stop the background service
   */
  private void stopAndCancelNotification() {
    Log.d(Constants.TAG, "shutting down!");
    stopForeground(true);
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
  private void handleCameraSliderMessages(byte[] message) {
    StringBuilder str = new StringBuilder();
    for (byte b : message) {
      str.append(String.format("%02X ", b));
    }
    Log.d(Constants.TAG, "msg received: " + str);
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

    // Asynchronously connect to the Bluetooth device
    ConnectThread connectThread = new ConnectThread(device, new ConnectThread.ConnectThreadListener() {
      @Override
      public void onConnect(BluetoothSocket socket) {
        Log.d(Constants.TAG, "connectThread listener onConnect");
        cameraSlider = new CameraSliderCommunicatorThread(socket, cameraSliderListener);
        cameraSlider.start();
      }

      @Override
      public void onConnectionFail() {
        Log.d(Constants.TAG, "couldn't connect to a device");
      }
    });

    connectThread.start();
  }

  // Handle CameraSliderCommunicatorThread messages.
  private CameraSliderCommunicatorThread.SocketListener cameraSliderListener = new CameraSliderCommunicatorThread.SocketListener() {
    // Called when a device is successfully connected and it is detected
    @Override
    public void onConnect(String deviceAddress) {
      Log.i(Constants.TAG, "device connected");

      notificationCommunicator.displayInfoNotification(R.string.notification_connected);
      localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_CONNECTED));

      eventBus.post(new CameraSliderConnectedEvent());

      isDeviceConnected = true;
      connectedDeviceAddress = deviceAddress;
    }

    // Called when a Camera Slider is disconnected. Won't be called if disconnecting another device
    @Override
    public void onDisconnect() {
      if (isDeviceConnected) {
        Log.i(Constants.TAG, "device disconnected");
        localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_DISCONNECTED));

        // Because the device was previously connected, display a notification that tells to tap
        // to reconnect
        notificationCommunicator.displayTapToConnectNotification();

        eventBus.post(new CameraSliderDisconnectedEvent());
      }

      isDeviceConnected = false;
    }

    // Called when connecting to a device failed
    @Override
    public void onConnectionFailed() {
      isDeviceConnected = false;
      notificationCommunicator.displayInfoNotification(R.string.notification_not_connected);
      localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_CONNECTION_FAILED));
    }

    // Called when the connection was successful but the device was not detected as a Camera Slider
    @Override
    public void onDeviceDetectionFail() {
      isDeviceConnected = false;
      Log.i(Constants.TAG, "detection failed");
      notificationCommunicator.displayInfoNotification(R.string.notification_not_connected);
      localBroadcastManager.sendBroadcast(new Intent(INTENT_DEVICE_DETECTION_FAILED));
    }

    // Called every time the Bluetooth device sends a message
    @Override
    public void onNewMessage(byte[] message) {
      handleCameraSliderMessages(message);
    }
  };

  /**
   * Listen to actions from notifications
   */
  private BroadcastReceiver notificationBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      switch (action) {
        case NotificationCommunicator.INTENT_PAUSE_PLAY_BUTTON_PRESSED:
          break;
        case NotificationCommunicator.INTENT_STOP_BUTTON_PRESSED:
          break;
        case NotificationCommunicator.INTENT_RECONNECT:
          connectToDevice(connectedDeviceAddress);
          Toast.makeText(context, "Reconnecting...", Toast.LENGTH_SHORT).show();
          break;
      }
    }
  };

  @Subscribe
  public void onManualMoveButtonHoldEvent(ManualMoveButtonHoldEvent event) {

  }
}
