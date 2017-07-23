package fi.peltoset.mikko.cameraslider.bluetooth;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import fi.peltoset.mikko.cameraslider.interfaces.BluetoothServiceListener;

public class BluetoothServiceCommunicator {
  private BluetoothServiceListener listener;
  private Messenger serviceMessenger = null;

  private Activity context;
  private BluetoothDevice bluetoothDevice;

  private boolean isServiceBound = false;

  public BluetoothServiceCommunicator(Activity context, BluetoothServiceListener listener) {
    this.context = context;
    this.listener = listener;

    startService();
  }

  /**
   * Tell the Bluetooth service to try to connect to the given device.
   *
   * @param device
   */
  public void connect(BluetoothDevice device) {
    bluetoothDevice = device;

    Message msg = Message.obtain(null, BluetoothService.MESSAGE_CONNECT_TO_DEVICE, 0, 0);

    Bundle data = new Bundle();
    data.putString(BluetoothService.EXTRA_DEVICE_ADDRESS, device.getAddress());

    msg.setData(data);

    try {
      serviceMessenger.send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  /**
   * Start the Bluetooth service
   */
  private void startService() {
    // startService decouples the service from the Activity's lifecycle. This ensures the service
    // is kept running even when the Activity is destroyed. The service is stopped only when
    // stopService is called.
    context.startService(new Intent(context, BluetoothService.class));
  }

  /**
   * Bind to a running service
   */
  public void bindService() {
    if (!isServiceRunning(BluetoothService.class)) {
      startService();
    }

    isServiceBound = context.bindService(new Intent(context, BluetoothService.class), serviceConnection, Context.BIND_AUTO_CREATE);

    // Register a LocalBroadcastManager to receive messages from the background service
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BluetoothService.INTENT_DEVICE_CONNECTED);
    intentFilter.addAction(BluetoothService.INTENT_DEVICE_DISCONNECTED);
    intentFilter.addAction(BluetoothService.INTENT_DEVICE_DETECTION_FAILED);
    LocalBroadcastManager.getInstance(context).registerReceiver(bluetoothServiceBroadcastReceiver, intentFilter);
  }

  /**
   * Unbind the service
   */
  public void unbindService() {
    if (isServiceBound) {
      context.unbindService(serviceConnection);
    }

    LocalBroadcastManager.getInstance(context).unregisterReceiver(bluetoothServiceBroadcastReceiver);
  }

  /**
   * Stop service
   */
  public void stopService() {
    Message msg = Message.obtain(null, BluetoothService.MESSAGE_STOP, 0, 0);

    try {
      serviceMessenger.send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

//    context.stopService(new Intent(context, BluetoothService.class));
  }

  private void sendString(String message) {
    Message msg = Message.obtain(null, BluetoothService.MESSAGE_STRING, 0, 0);

    Bundle data = new Bundle();
    data.putString(BluetoothService.EXTRA_STRING_MESSAGE, message);

    msg.setData(data);

    try {
      serviceMessenger.send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  // Used to set up the Messenger for communicating with the Bluetooth device
  private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      serviceMessenger = new Messenger(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      serviceMessenger = null;
    }
  };

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

  // Handle messages received from the Bluetooth service
  private BroadcastReceiver bluetoothServiceBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case BluetoothService.INTENT_DEVICE_CONNECTED:
          listener.onDeviceConnected(bluetoothDevice);
          break;
        case BluetoothService.INTENT_DEVICE_DISCONNECTED:
          listener.onDeviceDisconnected();
          break;
        case BluetoothService.INTENT_DEVICE_DETECTION_FAILED:
          listener.onDeviceDetectionFailed();
          break;
      }
    }
  };
}
