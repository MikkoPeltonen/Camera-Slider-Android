package fi.peltoset.mikko.cameraslider.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import fi.peltoset.mikko.cameraslider.CameraSliderApplication;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.interfaces.BluetoothDeviceSelectionListener;

public class BluetoothDeviceSelectionFragment extends Fragment {
  public static final String TAG = "BLUETOOTH_DEVICE_SELECTION_FRAGMENT";

  private BluetoothDeviceSelectionListener listener;
  private BluetoothAdapter bluetoothAdapter;

  private ArrayAdapter<String> pairedBluetoothDevicesArrayAdapter;
  private ArrayAdapter<String> availableBluetoothDevicesArrayAdapter;

  private ArrayList<BluetoothDevice> pairedBluetoothDevices = new ArrayList<>();
  private ArrayList<BluetoothDevice> availableBluetoothDevices = new ArrayList<>();

  private static final int FAB_ICON_START = R.drawable.ic_search_white_48dp;
  private static final int FAB_ICON_SEARCHING = R.drawable.ic_bluetooth_searching_white_48dp;
  private static final int COARSE_LOCATION_REQUEST_CODE = 42;

  private ListView pairedBluetoothDevicesListView;
  private ListView availableBluetoothDevicesListView;
  private FloatingActionButton startStopSearchButton;
  private ProgressBar progressBarSearching;
  private TextView deviceNameTextView;
  private TextView deviceAddressTextView;
  private TextView connectionInfoTextView;

  private String deviceName;
  private String deviceAddress;

  private CameraSliderApplication app;

  public BluetoothDeviceSelectionFragment() {}

  public static BluetoothDeviceSelectionFragment newInstance() {
    BluetoothDeviceSelectionFragment fragment = new BluetoothDeviceSelectionFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    pairedBluetoothDevicesArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.listview_bluetooth_device, R.id.deviceName);
    availableBluetoothDevicesArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.listview_bluetooth_device, R.id.deviceName);

    app = (CameraSliderApplication) getActivity().getApplication();
    deviceName = app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_NAME, null);
    deviceAddress = app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, null);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_bluetooth_device_selection, container, false);

    pairedBluetoothDevicesListView = (ListView) view.findViewById(R.id.pairedBluetoothDevices);
    availableBluetoothDevicesListView = (ListView) view.findViewById(R.id.availableBluetoothDevices);
    startStopSearchButton = (FloatingActionButton) view.findViewById(R.id.searchBluetoothDevices);
    progressBarSearching = (ProgressBar) view.findViewById(R.id.progressBarSearching);
    deviceNameTextView = (TextView) view.findViewById(R.id.deviceName);
    deviceAddressTextView = (TextView) view.findViewById(R.id.deviceAddress);
    connectionInfoTextView = (TextView) view.findViewById(R.id.connectionInfo);

    if (deviceName != null && deviceAddress != null) {
      deviceNameTextView.setText(deviceName);
      deviceAddressTextView.setText(deviceAddress);
      connectionInfoTextView.setText("paired, not connected - tap to connect");
    } else {
      deviceAddressTextView.setVisibility(View.INVISIBLE);
      connectionInfoTextView.setVisibility(View.INVISIBLE);
      deviceNameTextView.setText("NO DEVICE");
    }

    pairedBluetoothDevicesListView.setAdapter(pairedBluetoothDevicesArrayAdapter);
    pairedBluetoothDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bluetoothAdapter.cancelDiscovery();

        listener.onBluetoothDeviceSelected(pairedBluetoothDevices.get(position));
      }
    });

    availableBluetoothDevicesListView.setAdapter(availableBluetoothDevicesArrayAdapter);
    availableBluetoothDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bluetoothAdapter.cancelDiscovery();

        listener.onBluetoothDeviceSelected(availableBluetoothDevices.get(position));
      }
    });

    // If permission is granted, register receivers to find devices
    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
      registerReceivers();
    }

    // Handle Bluetooth search button clicks
    startStopSearchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Check if coarse location permission is granted. If not, request for the permission.
        // This is required to receive ACTION_FOUND with a BroadcastReceiver.
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(getActivity(), new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION }, COARSE_LOCATION_REQUEST_CODE);
        } else {
          // Otherwise toggle discovery mode
          toggleDiscovery();
        }
      }
    });

    // Add already paired devices to the list
    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

    if (pairedDevices.size() > 0) {
      for (BluetoothDevice device : pairedDevices) {
        pairedBluetoothDevicesArrayAdapter.add(device.getName());
        pairedBluetoothDevices.add(device);
      }
    }

    return view;
  }

  public void deviceConnected() {
    deviceName = app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_NAME, null);
    deviceAddress = app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, null);

    deviceNameTextView.setVisibility(View.VISIBLE);
    deviceNameTextView.setText(deviceName);

    deviceAddressTextView.setVisibility(View.VISIBLE);
    deviceAddressTextView.setText(deviceAddress);

    connectionInfoTextView.setVisibility(View.VISIBLE);
    connectionInfoTextView.setText("device connected");
  }

  // Called when the coarse location permission is either granted or not
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
    if (requestCode == COARSE_LOCATION_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // After succesfully getting the permission, register the BroadcastReceivers and start discovery
        registerReceivers();
        toggleDiscovery();
      } else {
        Toast.makeText(getContext(), "We cannot search for devices without location permission.", Toast.LENGTH_SHORT).show();
      }
    }
  }

  /**
   * Register BroadcastReceivers to interact with Bluetooth actions
   */
  private void registerReceivers() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(BluetoothDevice.ACTION_FOUND);
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    getActivity().registerReceiver(broadcastReceiver, filter);
  }

  /**
   * Start or stop Bluetooth discovery
   */
  private void toggleDiscovery() {
    if (bluetoothAdapter.isDiscovering()) {
      bluetoothAdapter.cancelDiscovery();
      startStopSearchButton.setImageResource(FAB_ICON_START);
      progressBarSearching.setVisibility(View.INVISIBLE);
    } else {
      bluetoothAdapter.startDiscovery();
      startStopSearchButton.setImageResource(FAB_ICON_SEARCHING);
      progressBarSearching.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // Verify that the Activity implements the BluetoothDeviceSelectionListener interface
    if (context instanceof BluetoothDeviceSelectionListener) {
      listener = (BluetoothDeviceSelectionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement BluetoothDeviceSelectionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();

    listener = null;
    getActivity().unregisterReceiver(broadcastReceiver);
  }

  private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      // Found a new device
      if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // If state is BOND_BONDED, the device is already added in the list earlier
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
          for (BluetoothDevice myDevice : pairedBluetoothDevices) {
            if (myDevice.getAddress().equals(device.getAddress())) {
              return;
            }
          }

          for (BluetoothDevice myDevice : availableBluetoothDevices) {
            if (myDevice.getAddress().equals(device.getAddress())) {
              return;
            }
          }

          availableBluetoothDevicesArrayAdapter.add(device.getName());
          availableBluetoothDevices.add(device);
        }
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
        // Discovery finished
        startStopSearchButton.setImageResource(FAB_ICON_START);
        progressBarSearching.setVisibility(View.INVISIBLE);
      }
    }
  };
}
