package fi.peltoset.mikko.cameraslider.bluetooth;

import android.app.Activity;
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
  private boolean isBluetoothServiceBound = false;

  private Activity context;
  private BluetoothDevice bluetoothDevice;

  public BluetoothServiceCommunicator(Activity context, BluetoothServiceListener listener) {
    this.context = context;
    this.listener = listener;

    bind();
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
   * Bind the BluetoothServiceCommunicator to the service responsible for Bluetooth actions
   */
  private void bind() {
    context.bindService(new Intent(context, BluetoothService.class), serviceConnection, Context.BIND_AUTO_CREATE);

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
  private void unbind() {
    if (isBluetoothServiceBound) {
      context.unbindService(serviceConnection);
      isBluetoothServiceBound = false;
    }
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
      isBluetoothServiceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      serviceMessenger = null;
      isBluetoothServiceBound = false;
    }
  };

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
