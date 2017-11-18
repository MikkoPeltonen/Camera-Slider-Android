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
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;

public class CameraSliderCommunicator {
  private Activity context;
  private BluetoothService bluetoothService;
  private CameraSliderCommunicatorInterface listener;
  private boolean isConnected = false;
  private boolean isActionRunning = false;

  public interface CameraSliderCommunicatorInterface {
    void onConnect();
    void onDisconnect();
    void onVerificationFail();
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
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      bluetoothService = null;
    }
  };

  // Used to receive messages from the service
  private BroadcastReceiver serviceMessageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case "connected":
          isConnected = true;
          listener.onConnect();
          break;
        case "disconnect":
          isConnected = false;
          listener.onDisconnect();
          break;
        case "verification":
          isConnected = false;
          listener.onVerificationFail();
          break;
        case "msg":
          break;
      }
    }
  };

  /**
   * Parent activity lifecycle hook register to BroadcastReceivers
   */
  public void onResume() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("connected");

    LocalBroadcastManager.getInstance(context).registerReceiver(serviceMessageReceiver, intentFilter);
  }

  /**
   * Parent activity lifecycle hook to unregister BroadcastReceivers
   */
  public void onPause() {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(serviceMessageReceiver);
  }

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
      unbindService();
      bluetoothService.stopAndCancelNotification();
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
}
