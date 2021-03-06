package fi.peltoset.mikko.cameraslider.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import fi.peltoset.mikko.cameraslider.CameraSliderApplication;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.bluetooth.CameraSliderCommunicator;
import fi.peltoset.mikko.cameraslider.fragments.BluetoothDeviceSelectionFragment;
import fi.peltoset.mikko.cameraslider.fragments.ManualModeFragment;
import fi.peltoset.mikko.cameraslider.fragments.MotorizedMovementFragment;
import fi.peltoset.mikko.cameraslider.fragments.PanoramaFragment;
import fi.peltoset.mikko.cameraslider.fragments.SettingsFragment;
import fi.peltoset.mikko.cameraslider.interfaces.BluetoothDeviceSelectionListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.KeyframePOJO;

public class CameraSliderMainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
               BluetoothDeviceSelectionListener,
               ManualModeFragment.ManualModeListener,
               MotorizedMovementFragment.MotorizedMovementFragmentListener {

  private CameraSliderApplication app;
  private CameraSliderCommunicator cameraSliderCommunicator = null;
  private DrawerLayout drawer;
  private ProgressDialog progressDialog;
  private String activeFragmentTag = null;
  private FragmentManager fragmentManager = getSupportFragmentManager();
  private NavigationView navigationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera_slider_main);

    // Test for Bluetooth
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      Toast.makeText(this, "This device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
      finish();
    } else if (!bluetoothAdapter.isEnabled()) {
      bluetoothAdapter.enable();
    }

    app = (CameraSliderApplication) getApplication();
    drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayShowTitleEnabled(false);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    if (savedInstanceState == null) {
      activeFragmentTag = ManualModeFragment.class.getName();
    } else {
      activeFragmentTag = savedInstanceState.getString("ACTIVE_FRAGMENT", ManualModeFragment.class.getName());
    }

    try {
      Class<?> cls = Class.forName(activeFragmentTag);
      changeFragment(cls);

      int menuItem = 0;

      if (activeFragmentTag.equals(ManualModeFragment.class.getName())) {
        menuItem = R.id.nav_manual;
      } else if (activeFragmentTag.equals(MotorizedMovementFragment.class.getName())) {
        menuItem = R.id.nav_motorized_video;
      } else if (activeFragmentTag.equals(PanoramaFragment.class.getName())) {
        menuItem = R.id.nav_panorama;
      } else if (activeFragmentTag.equals(BluetoothDeviceSelectionFragment.class.getName())) {
        menuItem = R.id.nav_devices;
      } else if (activeFragmentTag.equals(SettingsFragment.class.getName())) {
        menuItem = R.id.nav_settings;
      }

      if (menuItem != 0) {
        navigationView.setCheckedItem(menuItem);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }


  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("ACTIVE_FRAGMENT", activeFragmentTag);
  }

  @Override
  protected void onResume() {
    super.onResume();

    cameraSliderCommunicator = new CameraSliderCommunicator(this, cameraSliderCommunicatorInterface);

    // onResume is called every time the Activity is brought into view. If the
    // CameraSliderCommunicator is initialized and the service is not bound then bind it now.
    if (cameraSliderCommunicator != null && !cameraSliderCommunicator.isServiceBound()) {
      cameraSliderCommunicator.bindService();
    }
  }

  @Override
  protected void onPause() {
    // When the Activity is stopped (exits the view), unbind the service to prevent leaks. This would
    // leave the service running in standalone mode in the background but after unbinding we check
    // if there is an active task running (like timelapse or video). If not, terminate the service.
    if (cameraSliderCommunicator != null) {
      if (!cameraSliderCommunicator.isActionRunning()) {
        // Disable Bluetooth and get rid of notification
        cameraSliderCommunicator.stopService();
      }

      cameraSliderCommunicator.unbindService();
    }

    super.onPause();
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
    // Get the right fragment class based on the selected navigation item
    Class<?> selectedFragmentClass;
    switch (item.getItemId()) {
      case R.id.nav_manual:
        selectedFragmentClass = ManualModeFragment.class;
        break;
      case R.id.nav_motorized_video:
        selectedFragmentClass = MotorizedMovementFragment.class;
        break;
      case R.id.nav_panorama:
        selectedFragmentClass = PanoramaFragment.class;
        break;
      case R.id.nav_devices:
        selectedFragmentClass = BluetoothDeviceSelectionFragment.class;
        break;
      case R.id.nav_settings:
        selectedFragmentClass = SettingsFragment.class;
        break;
      default:
        throw new RuntimeException("Unknown fragment");
    }

    changeFragment(selectedFragmentClass);

    drawer.closeDrawer(GravityCompat.START);

    return true;
  }

  private void changeFragment(Class<?> selectedFragmentClass) {
    try {
      Fragment newFragment = fragmentManager.findFragmentByTag(selectedFragmentClass.getName());
      if (newFragment == null) {
        newFragment = (Fragment) selectedFragmentClass.newInstance();
      }

      fragmentManager.beginTransaction().replace(
          R.id.content_frame, newFragment, selectedFragmentClass.getName()
      ).commit();
    } catch (Exception e) {
      Toast.makeText(this, "Weird, it failed?", Toast.LENGTH_SHORT).show();
    }
  }

  private CameraSliderCommunicator.CameraSliderCommunicatorInterface cameraSliderCommunicatorInterface =
      new CameraSliderCommunicator.CameraSliderCommunicatorInterface() {
    @Override
    public void onConnect() {
      Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
      hideConnectionProgressDialog();

      Fragment f = getSupportFragmentManager().findFragmentByTag(BluetoothDeviceSelectionFragment.class.getName());
      if (f != null && f instanceof BluetoothDeviceSelectionFragment) {
        BluetoothDeviceSelectionFragment bFragment = (BluetoothDeviceSelectionFragment) f;
        bFragment.deviceConnected();
      }
    }

    @Override
    public void onDisconnect() {
      Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
      hideConnectionProgressDialog();
    }

    @Override
    public void onVerificationFail() {
      Toast.makeText(getApplicationContext(), "Verification failed", Toast.LENGTH_SHORT).show();
      hideConnectionProgressDialog();
    }

    @Override
    public void onHomingDone() {
      // Notify the ManualModeFragment about successful homing.
      ManualModeFragment fragment = (ManualModeFragment) fragmentManager.findFragmentByTag(ManualModeFragment.class.getName());
      if (fragment != null) {
        fragment.onHomingDone();
      }
    }

    @Override
    public void onDataSent() {
      // Notify MotorizedMovementFragment about the Camera Slider having successfully received
      // settings and move instructions.
      MotorizedMovementFragment fragment = (MotorizedMovementFragment) fragmentManager.findFragmentByTag(MotorizedMovementFragment.class.getName()) ;
      if (fragment != null) {
        fragment.onDataSent();
      }
    }
  };

  /**
   * Check if the device is connected to a Camera Slider.
   *
   * @return true if connected, false otherwise
   */
  public boolean isCameraSliderConnected() {
    return cameraSliderCommunicator.isConnected();
  }



  /**
   * Callback from BluetoothDeviceSelectionFragment to connect to a device.
   *
   * @param device
   */
  @Override
  public void onBluetoothDeviceSelected(BluetoothDevice device) {
    progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
    progressDialog.setMessage("Connecting to device...");
    progressDialog.show();

    cameraSliderCommunicator.connect(device.getAddress());
  }

  @Override
  public void reconnect() {
    String deviceAddress = app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, null);
    if (deviceAddress != null) {
      progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
      progressDialog.setMessage("Connecting to device...");
      progressDialog.show();

      cameraSliderCommunicator.connect(deviceAddress);
    }
  }

  /**
   * Hide the connection dialog
   */
  private void hideConnectionProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
    }
  }


  /**********************************
   * Manual Mode Fragment interface *
   **********************************/

  @Override
  public void setHome() {
    cameraSliderCommunicator.setHome();
  }

  @Override
  public void goHome() {
    cameraSliderCommunicator.goHome();
  }

  @Override
  public void resetHome() {
    Toast.makeText(getApplicationContext(), "reset home", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void moveManually(ManualModeFragment.ManualMoveInstructions instructions) {
    cameraSliderCommunicator.moveManually(instructions);
  }

  @Override
  public void openDevicesTab() {
    navigationView.setCheckedItem(R.id.nav_devices);
    changeFragment(BluetoothDeviceSelectionFragment.class);
  }

  /*****************************************
   * Motorized Movement Fragment interface *
   *****************************************/

  @Override
  public void sendDataToCameraSlider(byte[] settings, ArrayList<KeyframePOJO> keyframes) {
    cameraSliderCommunicator.saveInstructions(settings, keyframes);
  }
}
