package fi.peltoset.mikko.cameraslider.activities;

import android.app.ProgressDialog;
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
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import fi.peltoset.mikko.cameraslider.CameraSliderApplication;
import fi.peltoset.mikko.cameraslider.bluetooth.BluetoothService;
import fi.peltoset.mikko.cameraslider.bluetooth.BluetoothServiceCommunicator;
import fi.peltoset.mikko.cameraslider.fragments.BluetoothDeviceSelectionFragment;
import fi.peltoset.mikko.cameraslider.fragments.ManualModeFragment;
import fi.peltoset.mikko.cameraslider.fragments.MotorizedMovementFragment;
import fi.peltoset.mikko.cameraslider.fragments.PanoramaFragment;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.fragments.SettingsFragment;
import fi.peltoset.mikko.cameraslider.interfaces.BluetoothDeviceSelectionListener;
import fi.peltoset.mikko.cameraslider.interfaces.BluetoothServiceListener;
import fi.peltoset.mikko.cameraslider.interfaces.NotificationCommunicatorListener;
import fi.peltoset.mikko.cameraslider.notifications.NotificationCommunicator;

public class CameraSliderMainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, BluetoothDeviceSelectionListener, NotificationCommunicatorListener {

  private BluetoothServiceCommunicator bluetoothServiceCommunicator = null;
  private NotificationCommunicator notificationCommunicator = null;

  private DrawerLayout drawer;

  private ProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera_slider_main);

    // Test for Bluetooth
    if (BluetoothAdapter.getDefaultAdapter() == null) {
      Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
      finish();
    }

    drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayShowTitleEnabled(false);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    navigationView.setCheckedItem(R.id.nav_motorized_video);
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_frame, new MotorizedMovementFragment()).commit();

    notificationCommunicator = new NotificationCommunicator(this);
    notificationCommunicator.displaySampleNotification();
  }

  @Override
  protected void onStart() {
    super.onStart();

    bluetoothServiceCommunicator = new BluetoothServiceCommunicator(this, new BluetoothServiceListener() {
      @Override
      public void onDeviceConnected(BluetoothDevice device) {
        CameraSliderApplication app = (CameraSliderApplication) getApplication();
        app.preferencesEditor.putString(app.PREFERENCES_EXTRA_DEVICE_NAME, device.getName());
        app.preferencesEditor.putString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, device.getAddress());
        app.preferencesEditor.commit();

        Toast.makeText(getApplicationContext(), "Camera Slider connected", Toast.LENGTH_SHORT).show();
        hideConnectionProgressDialog();
      }

      @Override
      public void onDeviceDisconnected() {
        Toast.makeText(getApplicationContext(), "Camera Slider disconnected", Toast.LENGTH_SHORT).show();
        hideConnectionProgressDialog();
      }

      @Override
      public void onDeviceDetectionFailed() {
        Toast.makeText(getApplicationContext(), "Couldn't connect the device", Toast.LENGTH_SHORT).show();
        hideConnectionProgressDialog();
      }
    });
  }

  @Override
  protected void onStop() {
    super.onStop();
    notificationCommunicator.onStop();
  }

  @Override
  public void onBackPressed() {
    // When back button is pressed, close the navigation drawer if it is open. Otherwise
    // continue as normal.
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  /**
   * Handle fragment changes from the navigation drawer.
   *
   * @param item
   * @return
   */
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();
    Fragment fragment = null;

    if (id == R.id.nav_manual) {
      fragment = new ManualModeFragment();
    } else if (id == R.id.nav_motorized_video) {
      fragment = new MotorizedMovementFragment();
    } else if (id == R.id.nav_panorama) {
      fragment = new PanoramaFragment();
    } else if (id == R.id.nav_devices) {
      fragment = new BluetoothDeviceSelectionFragment();
    } else if (id == R.id.nav_settings) {
      fragment = new SettingsFragment();
    }

    if (fragment != null) {
      FragmentManager fragmentManager = getSupportFragmentManager();
      fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);

    return true;
  }

  /**
   * Callback from BluetoothDeviceSelectionFragment to connect to a device.
   *
   * @param device
   */
  @Override
  public void onBluetoothDeviceSelected(BluetoothDevice device) {
    progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
    progressDialog.setMessage("Connecting device...");
    progressDialog.show();

    bluetoothServiceCommunicator.connect(device);
  }

  private void hideConnectionProgressDialog() {
    progressDialog.hide();
  }

  @Override
  public void onNotificationStartPauseButtonPressed() {
  }

  @Override
  public void onNotificationStopButtonPressed() {

  }
}
