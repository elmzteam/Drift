package com.elmz.drift;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elmz.drift.openbci.OpenBCIService;

public class MainActivity extends Activity
	implements NavigationDrawerFragment.NavigationDrawerCallbacks, LoginFragment.Listener {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private boolean deviceEnabled = false;
	private int mPosition;
	private LoginFragment mLoginFragment;
	private StatusFragment mStatusFragment;
	private HistoryFragment mHistoryFragment;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
					break;
				case UsbManager.ACTION_USB_DEVICE_DETACHED:
					stopService(new Intent(MainActivity.this, OpenBCIService.class));
					Log.d(getString(R.string.log_tag), "Service stopped");
					break;
			}
		}
	};

	private Handler serviceCallback = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(mPosition) {
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
		setContentView(R.layout.main_activity);

		mNavigationDrawerFragment = (NavigationDrawerFragment)
			getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(
			R.id.navigation_drawer,
			(DrawerLayout) findViewById(R.id.drawer_layout));
		// Start service
		final Intent service = new Intent(this, OpenBCIService.class);
		service.putExtra(OpenBCIService.TAG, new Messenger(serviceCallback));
		startService(service);
		// Display login screen
		switchView(-1);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position){
		// update the main content by replacing fragments
		switchView(position);
	}

	@Override
	public void home() {
		switchView(0);
	}

	public void switchView(int viewId) {
		mPosition = viewId;
		switch(viewId) {
			case -1: // Login
				if (mLoginFragment == null) {
					mLoginFragment = new LoginFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mLoginFragment).commit();
				break;
			case 0: // Status
				if (mStatusFragment == null) {
					mStatusFragment = new StatusFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mStatusFragment).commit();
				break;
			case 1: // History
				if (mHistoryFragment == null) {
					mHistoryFragment = new HistoryFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mHistoryFragment).commit();
				break;
			case 2: // Settings
				break;
		}
	}

	@Override
	public void onResume() {
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
	public void onPause() {
		super.onPause();
		unregisterReceiver(mUsbReceiver);
	}

	public void onSectionAttached(int number){
		switch(number){
			case -1:
				mTitle = getString(R.string.title_login);
			case 1:
				mTitle = getString(R.string.title_status);
				break;
			case 2:
				mTitle = getString(R.string.title_history);
				break;
			case 3:
				mTitle = getString(R.string.title_settings);
				break;
		}
	}

	public void restoreActionBar(){
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if(!mNavigationDrawerFragment.isDrawerOpen()){
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if(id == R.id.action_toggle){
			if (deviceEnabled) {
				stopService(new Intent(MainActivity.this, OpenBCIService.class));
			} else {
				final Intent service = new Intent(this, OpenBCIService.class);
				service.putExtra(OpenBCIService.TAG, new Messenger(serviceCallback));
				startService(service);
			}
			deviceEnabled = !deviceEnabled;
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section
		 * number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber){
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment(){
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
														 Bundle savedInstanceState){
			View rootView = inflater.inflate(R.layout.fragment_layout, container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity){
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(
					getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}

}
