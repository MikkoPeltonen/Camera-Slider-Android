package fi.peltoset.mikko.cameraslider.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class CameraSliderMainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, BluetoothDeviceSelectionListener {

  private CameraSliderApplication app;
  private BluetoothServiceCommunicator bluetoothServiceCommunicator = null;
  private DrawerLayout drawer;
  private ProgressDialog progressDialog;
  private String activeFragmentTag = null;

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

    FragmentManager fragmentManager = getSupportFragmentManager();

    if (savedInstanceState == null) {
      activeFragmentTag = ManualModeFragment.class.getName();

      Log.d(Constants.TAG, "KAKKAKUKKANEN");
      navigationView.setCheckedItem(R.id.nav_manual);
      fragmentManager.beginTransaction().add(R.id.content_frame, ManualModeFragment.newInstance(), activeFragmentTag).commit();

    } else {
      Log.d(Constants.TAG, "JUUSTOKÄRPÄNEN");
      activeFragmentTag = savedInstanceState.getString("ACTIVE_FRAGMENT", ManualModeFragment.class.getName());
      Log.d(Constants.TAG, "!!!!!!!!!!!!!!!!!!!!!" + savedInstanceState.getString("ACTIVE_FRAGMENT"));

      Fragment visibleFragment = null;
      if (activeFragmentTag.equals(ManualModeFragment.class.getName())) {
        visibleFragment = ManualModeFragment.newInstance();
      } else if (activeFragmentTag.equals(MotorizedMovementFragment.class.getName())) {
        visibleFragment = MotorizedMovementFragment.newInstance();
      } else if (activeFragmentTag.equals(PanoramaFragment.class.getName())) {
        visibleFragment = PanoramaFragment.newInstance();
      } else if (activeFragmentTag.equals(BluetoothDeviceSelectionFragment.class.getName())) {
        visibleFragment = BluetoothDeviceSelectionFragment.newInstance();
      } else if (activeFragmentTag.equals(SettingsFragment.class.getName())) {
        visibleFragment = SettingsFragment.newInstance();
      }

      if (visibleFragment != null) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fragmentManager.findFragmentByTag(activeFragmentTag) == null) {
          fragmentTransaction.add(R.id.content_frame, visibleFragment, activeFragmentTag);
        } else {
          fragmentTransaction.hide(visibleFragment);
        }

//        fragmentManager.beginTransaction().add(R.id.content_frame, visibleFragment, activeFragmentTag).commit();
      }
    }

    bluetoothServiceCommunicator = new BluetoothServiceCommunicator(this, new BluetoothServiceListener() {
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
    });
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

    Log.d(Constants.TAG, "CameraSliderMainActivity.onStop");

    // When the Activity is stopped (exits the view), unbind the service to prevent leaks. This would
    // leave the service running in standalone mode in the background but after unbinding we check
    // if there is an active task running (like timelapse or video). If not, terminate the service.
    if (bluetoothServiceCommunicator != null) {
      Log.d(Constants.TAG, "unbinding");
      bluetoothServiceCommunicator.unbindService();

      if (!bluetoothServiceCommunicator.isActionRunning()) {
        // Disable Bluetooth and get rid of notification
        Log.d(Constants.TAG, "not running, stopping");
        bluetoothServiceCommunicator.stopService();
      }
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


  Class<?>[] classes = {
      ManualModeFragment.class,
      MotorizedMovementFragment.class,
      PanoramaFragment.class,
      BluetoothDeviceSelectionFragment.class,
      SettingsFragment.class
  };

  /**
   * Handle fragment changes from the navigation drawer.
   *
   * @param item
   * @return
   */
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    FragmentManager fragmentManager = getSupportFragmentManager();

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

    // Go through all fragments and hide all others except the selected one. It shouldn't be visible
    // but test anyways for safety.
    for (Class<?> fragmentClass : classes) {
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
    progressDialog.setMessage("Connecting to device...");
    progressDialog.show();

    bluetoothServiceCommunicator.connect(device.getAddress());
  }

  /**
   * Hide the connection dialog
   */
  private void hideConnectionProgressDialog() {
    if (progressDialog != null) {
      progressDialog.hide();
    }
  }
}
