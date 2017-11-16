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
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import fi.peltoset.mikko.cameraslider.CameraSliderApplication;
import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.bluetooth.BluetoothServiceCommunicator;
import fi.peltoset.mikko.cameraslider.fragments.BluetoothDeviceSelectionFragment;
import fi.peltoset.mikko.cameraslider.fragments.ManualModeFragment;
import fi.peltoset.mikko.cameraslider.fragments.MotorizedMovementFragment;
import fi.peltoset.mikko.cameraslider.fragments.PanoramaFragment;
import fi.peltoset.mikko.cameraslider.fragments.SettingsFragment;
import fi.peltoset.mikko.cameraslider.interfaces.BluetoothDeviceSelectionListener;
import fi.peltoset.mikko.cameraslider.interfaces.BluetoothServiceListener;
import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;
import fi.peltoset.mikko.cameraslider.miscellaneous.KeyframePOJO;
import fi.peltoset.mikko.cameraslider.miscellaneous.Motor;
import fi.peltoset.mikko.cameraslider.miscellaneous.RotationDirection;

public class CameraSliderMainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, BluetoothDeviceSelectionListener,
    ManualModeFragment.ManualModeListener {

  private CameraSliderApplication app;
  private BluetoothServiceCommunicator bluetoothServiceCommunicator = null;
  private DrawerLayout drawer;
  private ProgressDialog progressDialog;
  private String activeFragmentTag = null;
  private FragmentManager fragmentManager = getSupportFragmentManager();

  // Used to detect if the activity is restarted by the system or by the user. If set to false,
  // the system has restarted the app (e.g. because of a configuration change). In this case the
  // app should not try to connect to a Bluetooth device if it didn't succeed last time.
  private boolean isFirstLaunch = true;

  private Class<?>[] fragmentClasses = {
      ManualModeFragment.class,
      MotorizedMovementFragment.class,
      PanoramaFragment.class,
      BluetoothDeviceSelectionFragment.class,
      SettingsFragment.class
  };

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

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    if (savedInstanceState == null) {
      activeFragmentTag = ManualModeFragment.class.getName();
    } else {
      activeFragmentTag = savedInstanceState.getString("ACTIVE_FRAGMENT", ManualModeFragment.class.getName());
      isFirstLaunch = false;
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

    bluetoothServiceCommunicator = new BluetoothServiceCommunicator(this, bluetoothServiceListener);
  }


  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("ACTIVE_FRAGMENT", activeFragmentTag);
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(Constants.TAG, "CameraSliderMainActivity.onStart");

//    EventBus.getDefault().register(this);

    // onStart is called every time the Activity is brought into view. If the BluetoothServiceCommunicator
    // is initialized and the service is not bound then bind it now.
    if (bluetoothServiceCommunicator != null && !bluetoothServiceCommunicator.isServiceBound()) {
      Log.d(Constants.TAG, "service not bound, binding...");
      bluetoothServiceCommunicator.bindService();
    }

    // If a Bluetooth device is saved in the settings and no devide is currently connected, try
    // to connect to the saved device on startup.
    if (app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, null) != null && !bluetoothServiceCommunicator.isDeviceConnected()) {
//      bluetoothServiceCommunicator.connect(app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, null));
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

//    EventBus.getDefault().unregister(this);

    Log.d(Constants.TAG, "CameraSliderMainActivity.onStop");

    // When the Activity is stopped (exits the view), unbind the service to prevent leaks. This would
    // leave the service running in standalone mode in the background but after unbinding we check
    // if there is an active task running (like timelapse or video). If not, terminate the service.
    if (bluetoothServiceCommunicator != null) {
      if (!bluetoothServiceCommunicator.isActionRunning()) {
        // Disable Bluetooth and get rid of notification
        Log.d(Constants.TAG, "not running, stopping");
        bluetoothServiceCommunicator.stopService();
      }

      Log.d(Constants.TAG, "unbinding");
      bluetoothServiceCommunicator.unbindService();
    }
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
    // Go through all fragments and hide all others except the selected one. It shouldn't be visible
    // but test anyways for safety.
    for (Class<?> fragmentClass : fragmentClasses) {
      String fragmentClassName = fragmentClass.getName();

      if (fragmentClassName.equals(selectedFragmentClass.getName())) {
        continue;
      }

      Fragment testedFragment = fragmentManager.findFragmentByTag(fragmentClassName);
      if (testedFragment != null) {
        fragmentManager.beginTransaction().hide(testedFragment).commit();
      }
    }

    // If a fragment is not found with the selected fragments class (tag), then instantiate a new
    // fragment with the name and add it to the fragment manager. Otherwise show the fragment.
    Fragment newFragment = fragmentManager.findFragmentByTag(selectedFragmentClass.getName());
    if (newFragment == null) {
      try {
        fragmentManager.beginTransaction().add(R.id.content_frame, (Fragment) Class.forName(selectedFragmentClass.getName()).newInstance(), selectedFragmentClass.getName()).commit();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      fragmentManager.beginTransaction().show(newFragment).commit();
    }
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

    bluetoothServiceCommunicator.connect(device.getAddress());
  }

  @Override
  public void reconnect() {
    String deviceAddress = app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, null);
    if (deviceAddress != null) {
      progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
      progressDialog.setMessage("Connecting to device...");
      progressDialog.show();

      bluetoothServiceCommunicator.connect(deviceAddress);
    }
  }

  /**
   * Hide the connection dialog
   */
  private void hideConnectionProgressDialog() {
    if (progressDialog != null) {
      progressDialog.hide();
    }
  }

  private BluetoothServiceListener bluetoothServiceListener = new BluetoothServiceListener() {
    @Override
    public void onServiceBound() {
      Log.d(Constants.TAG, "onServiceBound");

      if (!isFirstLaunch) {
        return;
      }

      // If a device is saved, try to connect to it on launch.
      String deviceAddress = app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, null);
      if (deviceAddress != null && !bluetoothServiceCommunicator.isDeviceConnected()) {
        bluetoothServiceCommunicator.connect(app.preferences.getString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, null));
      }
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
      CameraSliderApplication app = (CameraSliderApplication) getApplication();
      app.preferencesEditor.putString(app.PREFERENCES_EXTRA_DEVICE_NAME, device.getName());
      app.preferencesEditor.putString(app.PREFERENCES_EXTRA_DEVICE_ADDRESS, device.getAddress());
      app.preferencesEditor.commit();

      Toast.makeText(getApplicationContext(), "Camera Slider connected", Toast.LENGTH_SHORT).show();
      hideConnectionProgressDialog();

      Fragment f = getSupportFragmentManager().findFragmentByTag(BluetoothDeviceSelectionFragment.class.getName());
      if (f != null && f instanceof BluetoothDeviceSelectionFragment) {
        BluetoothDeviceSelectionFragment bFragment = (BluetoothDeviceSelectionFragment) f;
        bFragment.deviceConnected();
      }
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

    @Override
    public void onDeviceConnectionFailed() {
      Toast.makeText(getApplicationContext(), "Couldn't connect to the device", Toast.LENGTH_SHORT).show();
      hideConnectionProgressDialog();
    }
  };



  /**********************************
   * Manual Mode Fragment interface *
   **********************************/

  @Override
  public void setHome(KeyframePOJO home) {
    Toast.makeText(getApplicationContext(), "set home", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void goHome() {
    Toast.makeText(getApplicationContext(), "go home", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void resetHome() {
    Toast.makeText(getApplicationContext(), "reset home", Toast.LENGTH_SHORT).show();
  }
}
