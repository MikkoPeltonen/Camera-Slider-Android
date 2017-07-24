package fi.peltoset.mikko.cameraslider.bluetooth;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
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
import android.util.Log;

import fi.peltoset.mikko.cameraslider.interfaces.BluetoothServiceListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;

public class BluetoothServiceCommunicator {
  private BluetoothServiceListener listener;
  private Messenger serviceMessenger = null;

  private Activity context;
  private BluetoothDevice bluetoothDevice;

  private BluetoothAdapter bluetoothAdapter;

  private boolean isServiceBound = false;
  private boolean isDeviceConnected = false;
  private boolean connectOnBind = false;

  public BluetoothServiceCommunicator(Activity context, BluetoothServiceListener listener) {
    this.context = context;
    this.listener = listener;

    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    startService();
  }

  public boolean isServiceBound() {
    return isServiceBound;
  }

  public boolean isDeviceConnected() {
    return isDeviceConnected;
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

    bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

    Message msg = Message.obtain(null, BluetoothService.MESSAGE_CONNECT_TO_DEVICE, 0, 0);

    Bundle data = new Bundle();
    data.putString(BluetoothService.EXTRA_DEVICE_ADDRESS, bluetoothDevice.getAddress());

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
    if (!isServiceRunning(BluetoothService.class)) {
      context.startService(new Intent(context, BluetoothService.class));
    }
  }

  /**
   * Bind to a running service
   */
  public void bindService() {
    Log.d(Constants.TAG, "BluetoothServiceCommunicator.bindService");
    if (!isServiceRunning(BluetoothService.class)) {
      Log.d(Constants.TAG, "service not running, why!?");
      startService();
    } else {
      Log.d(Constants.TAG, "service running, binding and registering BroadcastReceivers");
      isServiceBound = context.bindService(new Intent(context, BluetoothService.class), serviceConnection, Context.BIND_AUTO_CREATE);

      // Register a LocalBroadcastManager to receive messages from the background service
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(BluetoothService.INTENT_DEVICE_CONNECTED);
      intentFilter.addAction(BluetoothService.INTENT_DEVICE_DISCONNECTED);
      intentFilter.addAction(BluetoothService.INTENT_DEVICE_DETECTION_FAILED);
      LocalBroadcastManager.getInstance(context).registerReceiver(bluetoothServiceBroadcastReceiver, intentFilter);
    }
  }

  /**
   * Unbind the service
   */
  public void unbindService() {
    Log.d(Constants.TAG, "BluetoothServiceCommunicator.unbindService");
    if (isServiceBound) {
      Log.d(Constants.TAG, "bound, unbinding");
      context.unbindService(serviceConnection);
      isServiceBound = false;
    }

    LocalBroadcastManager.getInstance(context).unregisterReceiver(bluetoothServiceBroadcastReceiver);
  }

  /**
   * Stop service
   */
  public void stopService() {
    Log.d(Constants.TAG, "BluetoothServiceCommunicator.stopService");
    Message msg = Message.obtain(null, BluetoothService.MESSAGE_STOP, 0, 0);

    try {
      serviceMessenger.send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
    }

//    context.stopService(new Intent(context, BluetoothService.class));
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
