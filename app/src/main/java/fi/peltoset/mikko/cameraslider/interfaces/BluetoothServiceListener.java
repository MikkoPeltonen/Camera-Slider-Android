package fi.peltoset.mikko.cameraslider.interfaces;

import android.bluetooth.BluetoothDevice;

public interface BluetoothServiceListener {
  void onDeviceConnected(BluetoothDevice device);
  void onDeviceDisconnected();
  void onDeviceDetectionFailed();
}
