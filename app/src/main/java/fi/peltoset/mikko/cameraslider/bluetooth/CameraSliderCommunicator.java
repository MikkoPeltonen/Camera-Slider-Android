package fi.peltoset.mikko.cameraslider.bluetooth;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import fi.peltoset.mikko.cameraslider.fragments.ManualModeFragment;
import fi.peltoset.mikko.cameraslider.miscellaneous.Axis;
import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;
import fi.peltoset.mikko.cameraslider.miscellaneous.Helpers;
import fi.peltoset.mikko.cameraslider.miscellaneous.KeyframePOJO;
import fi.peltoset.mikko.cameraslider.miscellaneous.RotationDirection;

public class CameraSliderCommunicator {
  private Activity context;
  private BluetoothService bluetoothService;
  private CameraSliderCommunicatorInterface listener;

  private boolean isConnected = false;
  private boolean isActionRunning = false;
  private boolean isHoming = false;

  public interface CameraSliderCommunicatorInterface {
    void onConnect();
    void onDisconnect();
    void onVerificationFail();
    void onHomingDone();
    void onDataSent();
  }

  public CameraSliderCommunicator(Activity context, CameraSliderCommunicatorInterface listener) {
    this.context = context;
    this.listener = listener;

    startService();
  }

  // Used to set up the Messenger for communicating with the Bluetooth device
  private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      bluetoothService = ((BluetoothService.LocalBinder) service).getService();
      bluetoothService.addListener(serviceMessenger);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      bluetoothService = null;
    }
  };

  // Used to receive messages from the service. A Messenger or a BroadcastReceiver must be used
  // to get data back from the Service because they operate in different threads. A interface
  // callback works but won't run on the UI thread which results into problems later on.
  private class ServiceMessageHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case BluetoothService.MSG_CONNECTED:
          isConnected = true;
          listener.onConnect();
          verificationHandler.post(verificationRunnable);
          break;
        case BluetoothService.MSG_DISCONNECTED:
          isConnected = false;
          listener.onDisconnect();
          verificationHandler.removeCallbacks(verificationRunnable);
          break;
        case BluetoothService.MSG_VERIFICATION_FAIL:
          isConnected = false;
          listener.onVerificationFail();
          break;
        case BluetoothService.MSG_MESSAGE:
          processMessage(msg.getData().getByteArray(BluetoothService.MSG_MESSAGE_DATA_MESSAGE));
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }

  private final Messenger serviceMessenger = new Messenger(new ServiceMessageHandler());

  // When Camera Slider is connected, it expects a handshake message every VERIFICATION_INTERVAL
  // milliseconds to confirm the connection is still online. This is because the Bluetooth module
  // on Arduino has no way of knowing if the socket is still open or not. If a message is not
  // received often enough, the connection status is set to disconnected on Arduino. After that
  // Android needs to re-establish the connection by going trough the handshake verification
  // process.
  private Handler verificationHandler = new Handler();
  private Runnable verificationRunnable = new Runnable() {
    @Override
    public void run() {
      bluetoothService.sendCommand(ConnectionConstants.SEND_VERIFICATION, new byte[]{});
      verificationHandler.postDelayed(this, ConnectionConstants.VERIFICATION_INTERVAL);
    }
  };

  /**
   * Start the Bluetooth service
   */
  public void startService() {
    // startService decouples the service from the Activity's lifecycle. This ensures the service
    // is kept running even when the Activity is destroyed. The service is stopped only when
    // stopService is called.
    if (!isServiceRunning(BluetoothService.class)) {
      context.startService(new Intent(context, BluetoothService.class));
    }
  }

  /**
   * Stop service. Requires a bound Activity.
   */
  public void stopService() {
    if (isServiceRunning(BluetoothService.class) && bluetoothService != null) {
      bluetoothService.stopAndCancelNotification();
      unbindService();
    }
  }

  /**
   * Bind to a running service
   */
  public void bindService() {
    if (!isServiceRunning(BluetoothService.class)) {
      throw new RuntimeException("Service must be running before calling CameraSliderCommunicator.bindService()");
    } else {
      context.bindService(new Intent(context, BluetoothService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }
  }

  /**
   * Unbind the service
   */
  public void unbindService() {
    if (bluetoothService != null) {
      context.unbindService(serviceConnection);
      bluetoothService = null;
    }
  }

  /**
   * Handle incoming messages from BluetoothService (Camera Slider).
   *
   * @param message Bundled message
   */
  private void processMessage(byte[] message) {
    byte command = message[1];

    switch (command) {
      case ConnectionConstants.HOMING_DONE:
        isHoming = false;
        listener.onHomingDone();
        break;
      case ConnectionConstants.DATA_RECEIVED:
        listener.onDataSent();
        break;
    }
  }

  /**
   * Tell the Bluetooth service to try to connect to the given device.
   *
   * @param deviceAddress
   */
  public void connect(String deviceAddress) {
    if (!BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
      return;
    }

    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

    bluetoothService.connectToDevice(deviceAddress);
  }

  /**
   * Check whether a service is already running or not
   *
   * @param serviceClass
   * @return
   */
  private boolean isServiceRunning(Class<?> serviceClass) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Check whether an activity is bound to the service or not.
   *
   * @return
   */
  public boolean isServiceBound() {
    return bluetoothService != null;
  }

  /**
   * Check if an action is running on the device.
   *
   * @return
   */
  public boolean isActionRunning() {
    return isActionRunning;
  }

  /**
   * Check if Android is connected to a verified Camera Slider.
   *
   * @return
   */
  public boolean isConnected() {
    return isConnected;
  }


  private byte setBit(byte b, int nthBit, boolean on) {
    return (byte) (b ^ ((-(on ? 1 : 0) ^ b) & (1 << (7 - nthBit))));
  }

  private byte setAxisInstructions(byte b, Axis axis, RotationDirection direction) {
    int firstBit = 0;
    if (axis == Axis.SLIDE || axis == Axis.FOCUS) {
      firstBit = 0;
    } else if (axis == Axis.PAN || axis == Axis.ZOOM) {
      firstBit = 2;
    } else if (axis == Axis.TILT) {
      firstBit = 4;
    }

    boolean isMoving = direction != RotationDirection.STOP;
    boolean isCW = isMoving && direction == RotationDirection.CW;

    byte returnByte = b;
    returnByte = setBit(returnByte, firstBit, isCW);
    returnByte = setBit(returnByte, firstBit + 1, isMoving);

    return returnByte;
  }

  /**********************
   *                    *
   * Operation commands *
   *                    *
   **********************/

  /**
   * Move the Camera Slider to the set home position.
   */
  public void goHome() {
    bluetoothService.sendCommand(ConnectionConstants.GO_HOME, new byte[]{});
    isHoming = true;
  }

  /**
   * Set the current position as the home position.
   */
  public void setHome() {
    bluetoothService.sendCommand(ConnectionConstants.SET_HOME, new byte[]{});
  }

  /**
   * Send command to move the motors manually
   *
   * @param instructions
   */
  public void moveManually(ManualModeFragment.ManualMoveInstructions instructions) {
    byte[] payload = { 0, 0 };

    payload[0] = setAxisInstructions(payload[0], Axis.SLIDE, instructions.slide);
    payload[0] = setAxisInstructions(payload[0], Axis.PAN, instructions.pan);
    payload[0] = setAxisInstructions(payload[0], Axis.TILT, instructions.tilt);
    payload[1] = setAxisInstructions(payload[1], Axis.FOCUS, instructions.focus);
    payload[1] = setAxisInstructions(payload[1], Axis.ZOOM, instructions.zoom);

    bluetoothService.sendCommand(ConnectionConstants.MOVE_MOTORS, payload);
  }

  /**
   * Send scene settings and keyframes to the Camera Slider.
   *
   * @param settings
   * @param keyframes
   */
  public void saveInstructions(byte[] settings, ArrayList<KeyframePOJO> keyframes) {
    // Create the first command to tell the Camera Slider what to expect.
    ByteArrayOutputStream beginPayloadBuffer = new ByteArrayOutputStream();

    beginPayloadBuffer.write(Helpers.intToByteArray(settings.length), 0, 4);
    beginPayloadBuffer.write(Helpers.intToByteArray(keyframes.size()), 0, 4);

    bluetoothService.sendCommand(ConnectionConstants.BEGIN_DATA_DOWNLOAD, beginPayloadBuffer.toByteArray());

    // Send the settings
    ByteArrayOutputStream settingsBuffer = new ByteArrayOutputStream();
    bluetoothService.sendCommand(ConnectionConstants.SAVE_SETTINGS, settingsBuffer.toByteArray());

    // Send all the keyframe instructions one by one.
    for (KeyframePOJO keyframe : keyframes) {
      bluetoothService.sendCommand(ConnectionConstants.SAVE_INSTRUCTIONS, keyframe.toByteArray());
    }

    // Send a checksum of the data to verify it.
    ByteArrayOutputStream checksumBuffer = new ByteArrayOutputStream();
    bluetoothService.sendCommand(ConnectionConstants.SEND_DATA_CHECKSUM, checksumBuffer.toByteArray());
  }
}
