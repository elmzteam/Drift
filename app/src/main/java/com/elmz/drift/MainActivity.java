package com.elmz.drift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.elmz.drift.drawer.AbstractDrawerActivity;
import com.elmz.drift.drawer.NavDrawerActivityConfig;
import com.elmz.drift.drawer.NavDrawerAdapter;
import com.elmz.drift.drawer.NavMenuBuilder;
import com.elmz.drift.drawer.NavMenuItem;
import com.elmz.drift.openbci.OpenBCIService;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AbstractDrawerActivity implements LoginFragment.Listener{

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private boolean deviceEnabled = false;
	private int mPosition;
	private LoginFragment mLoginFragment;
	private StatusFragment mStatusFragment;
	private HistoryFragment mHistoryFragment;

	private int tripId = -1;

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			switch(intent.getAction()){
				case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
					break;
				case UsbManager.ACTION_USB_DEVICE_DETACHED:
					stopService(new Intent(MainActivity.this, OpenBCIService.class));
					Log.d(getString(R.string.log_tag), "Service stopped");
					break;
			}
		}
	};

	// Callback from OpenBCI service
	private Handler serviceCallback = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(mPosition){
				case -1: // Login view
					mLoginFragment.onDevice(msg.arg1 == 1);
					break;
				case 0: // Status view

					break;
				case 1: // History view

					break;
				case 2: // Settings view
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		// Start service
		final Intent service = new Intent(this, OpenBCIService.class);
		service.putExtra(OpenBCIService.TAG, new Messenger(serviceCallback));
		startService(service);
		// Display login screen
		switchView(-1);
	}

	@Override
	public void home(){
		switchView(0);
	}

	@Override
	public NavDrawerActivityConfig getNavDrawerConfiguration(){
		final NavDrawerAdapter adapter = new NavDrawerAdapter(this, R.layout.nav_item);
		adapter.setItems(new NavMenuBuilder()
			.addItem(NavMenuItem.create(0, "Status", R.drawable.ic_done))
			.addItem(NavMenuItem.create(1, "History", R.drawable.ic_done))
			.addSeparator()
			.addItem(NavMenuItem.createButton(2, "Settings", R.drawable.ic_settings_black_24dp))
			.addItem(NavMenuItem.createButton(3, "Logout", R.drawable.ic_exit_to_app_black_24dp))
			.build());

		return new NavDrawerActivityConfig.Builder()
			.mainLayout(R.layout.drawer_layout)
			.drawerLayoutId(R.id.drawer_layout)
			.drawerContainerId(R.id.drawer_container)
			.leftDrawerId(R.id.drawer)
			.checkedPosition(0)
			.drawerShadow(R.drawable.drawer_shadow)
			.drawerOpenDesc(R.string.action_drawer_open)
			.drawerCloseDesc(R.string.action_drawer_close)
			.adapter(adapter)
			.build();
	}

	@Override
	public void onNavItemSelected(int id){
		switch(id){
			case 0:
			case 1:
				switchView(id);
				break;
			case 2:
				break;
			case 3:
				SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.remove("authToken");
				editor.remove("username");
				editor.commit();
				mLoginFragment = new LoginFragment();
				switchView(-1);
				break;
		}
	}

	public void switchView(int viewId){
		if(mPosition == -1){
			super.getDrawerToggle().setDrawerIndicatorEnabled(true);
			deviceEnabled = true;
			invalidateOptionsMenu();
		}
		mPosition = viewId;
		switch(viewId){
			case -1: // Login
				if(mLoginFragment == null){
					mLoginFragment = new LoginFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mLoginFragment).commit();
				super.getDrawerToggle().setDrawerIndicatorEnabled(false);
				invalidateOptionsMenu();
				break;
			case 0: // Status
				if(mStatusFragment == null){
					mStatusFragment = new StatusFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mStatusFragment).commit();
				break;
			case 1: // History
				if(mHistoryFragment == null){
					mHistoryFragment = new HistoryFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mHistoryFragment).commit();
				break;
			case 2: // Settings
				break;
			case 3: // Logout
				break;
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		// OpenBCI connection receiver
		final IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		filter.setPriority(500);
		registerReceiver(mUsbReceiver, filter);
		Log.d(getString(R.string.log_tag), "Registered receiver");
	}

	@Override
	public void onPause(){
		super.onPause();
		unregisterReceiver(mUsbReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if(mPosition != -1){
			getMenuInflater().inflate(R.menu.main, menu);
			if(deviceEnabled){
				menu.findItem(R.id.action_toggle).setIcon(R.drawable.ic_pause_circle_fill_white_48dp);
			} else {
				menu.findItem(R.id.action_toggle).setIcon(R.drawable.ic_play_circle_fill_white_48dp);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if(id == R.id.action_toggle){
			if(deviceEnabled){
				item.setIcon(R.drawable.ic_pause_circle_fill_white_48dp);
				stopService(new Intent(this, OpenBCIService.class));
				endDrive();
			} else {
				item.setIcon(R.drawable.ic_play_circle_fill_white_48dp);
				startDrive();
			}
			final Intent service = new Intent(this, OpenBCIService.class);
			service.putExtra(OpenBCIService.TAG, new Messenger(serviceCallback));
			if(startService(service) != null){
				stopService(service);
			}
			deviceEnabled = !deviceEnabled;
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void startDrive(){
		Log.d(getString(R.string.log_tag), "starting drive");

		final String startLoc = getCityName();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		final long startTime = cal.getTimeInMillis();

		final SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		final String username = sp.getString("username", "");
		final String authToken = sp.getString("authToken", "");

		APIRequestFragment arf = new APIRequestFragment(new ICallback(){
			@Override
			public void callback(JsonElement arg){
				int tripId = arg.getAsInt();

				APIRequestFragment arf2 = new APIRequestFragment(new ICallback(){
					@Override
					public void callback(JsonElement arg){

					}
				});

				arf2.execute("POST", "setStart", username, authToken, Integer.toString(tripId), Long.toString(startTime), startLoc);

				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("tripId", tripId);
				editor.apply();
			}
		});

		arf.execute("POST", "createTrip", username, authToken);
	}

	public void endDrive(){
		Log.d(getString(R.string.log_tag), "ending drive");

		final String endLoc = getCityName();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		final long endTime = cal.getTimeInMillis();

		final SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		final String username = sp.getString("username", "");
		final String authToken = sp.getString("authToken", "");

		final int drowsinessScore = 38; // TODO

		APIRequestFragment arf = new APIRequestFragment(new ICallback(){
			@Override
			public void callback(JsonElement arg){
				SharedPreferences.Editor editor = sp.edit();
				editor.remove("tripId");
				editor.apply();
			}
		});

		arf.execute("POST", "setEnd", username, authToken, Integer.toString(sp.getInt("tripId", -1)), Long.toString(endTime), endLoc, Integer.toString(drowsinessScore));
	}

	private String getCityName(){
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();

		String cityName = "Not Found";
		Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
		try{
			List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
			if(addresses.size() > 0){
				cityName = addresses.get(0).getLocality();
				// you should also try with addresses.get(0).toSring();
				System.out.println(cityName);
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		return cityName;
	}
}
