package fi.peltoset.mikko.cameraslider.interfaces;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDeviceSelectionListener {
  void onBluetoothDeviceSelected(BluetoothDevice device);
  void reconnect();
}