package com.elmz.drift;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.elmz.drift.openbci.OpenBCIService;
import com.google.gson.JsonElement;

public class LoginConnectActivity extends Activity
{
	private EditText inpUsername;
	private EditText inpPassword;
	private Button btnLoginSubmit;
	private ProgressBar loginSpinner;
	private TextView loginStatus;

	private boolean loginOk = false;
	private boolean deviceOk = false;

	private SharedPreferences sp;

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
					final Intent service = new Intent(LoginConnectActivity.this, OpenBCIService.class);
					service.putExtra(OpenBCIService.TAG, new Messenger(serviceCallback));
					startService(service);
					Log.d(getString(R.string.log_tag), "Service started");
					break;
				case UsbManager.ACTION_USB_DEVICE_DETACHED:
					stopService(new Intent(LoginConnectActivity.this, OpenBCIService.class));
					Log.d(getString(R.string.log_tag), "Service stopped");
					break;
			}
		}
	};

	private Handler serviceCallback = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			onDevice("what?");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_connect);

		sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

		inpUsername = (EditText) findViewById(R.id.text_input_username);
		inpPassword = (EditText) findViewById(R.id.text_input_password);
		btnLoginSubmit = (Button) findViewById(R.id.btn_login_submit);
		loginSpinner = (ProgressBar) findViewById(R.id.prog_login_spinner);
		loginStatus = (TextView) findViewById(R.id.text_login_status);

		btnLoginSubmit.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				inpUsername.setEnabled(false);
				inpPassword.setEnabled(false);
				btnLoginSubmit.setEnabled(false);
				btnLoginSubmit.setVisibility(View.GONE);
				loginSpinner.setVisibility(View.VISIBLE);

				loginStatus.setText("Authorizing...");

				final String username = inpUsername.getText().toString();
				final String password = inpPassword.getText().toString();

				APIRequestFragment arf = new APIRequestFragment(new ICallback(){
					@Override
					public void callback(JsonElement tok){
						String token;
						if(!tok.isJsonNull() && (token = tok.getAsString()).length() > 0){
							if(token.length() > 0){
							Log.d(getString(R.string.log_tag), "Login accepted");

							SharedPreferences.Editor editor = sp.edit();
							editor.putString("authToken", token);
							editor.putString("username", username);
							editor.commit();
							onLogin();
						} else {
							Log.d(getString(R.string.log_tag), "Login rejected");
							loginStatus.setText("Invalid username or password.");


							inpUsername.setEnabled(true);
							inpPassword.setEnabled(true);
							btnLoginSubmit.setEnabled(true);
							btnLoginSubmit.setVisibility(View.VISIBLE);
							loginSpinner.setVisibility(View.GONE);
						}
					}
				});

				arf.execute("POST", "authUser", username, password);
			}
		});

		if(sp.getString("authToken", "").length() > 0 && sp.getString("username", "").length() > 0){
			loginStatus.setText("Checking stored authorization...");

			inpUsername.setEnabled(false);
			inpPassword.setEnabled(false);
			btnLoginSubmit.setEnabled(false);
			btnLoginSubmit.setVisibility(View.GONE);
			loginSpinner.setVisibility(View.VISIBLE);

			APIRequestFragment arf = new APIRequestFragment(new ICallback(){
				@Override
				public void callback(JsonElement arg){
					String status;
					if(!arg.isJsonNull()) {
						status = arg.getAsString();
						Log.d(getString(R.string.log_tag), "Auth validation: " + status);
						if(status.equals("true")){
							onLogin();
						} else {
							loginStatus.setText("Invalid stored authorization.");

							inpUsername.setEnabled(true);
							inpPassword.setEnabled(true);
							btnLoginSubmit.setEnabled(true);
							btnLoginSubmit.setVisibility(View.VISIBLE);
							loginSpinner.setVisibility(View.GONE);
						}
					} else {
						Log.d(getString(R.string.log_tag), "Auth returned null");
					}
				}
			});

			arf.execute("POST", "isValid", sp.getString("username", ""), sp.getString("authToken", ""));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mUsbReceiver);
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

	private void onLogin(){
		loginStatus.setText("Authorized.");

		findViewById(R.id.ic_done_login).setVisibility(View.VISIBLE);
		loginStatus.requestFocus();
		loginOk = true;
		loginSpinner.setVisibility(View.GONE);
		checkForCompleteness();
	}

	private void onDevice(String id){
		// TODO: Anything
		findViewById(R.id.ic_done_connect).setVisibility(View.VISIBLE);
		findViewById(R.id.message_no_device).setVisibility(View.GONE);
		findViewById(R.id.message_device_found).setVisibility(View.VISIBLE);
		((TextView) (findViewById(R.id.text_device_id))).setText(id);
		deviceOk = true;
		checkForCompleteness();
	}

	private void checkForCompleteness(){
		if(loginOk && deviceOk){
			Log.d(getString(R.string.log_tag), "Passing to MainActivity");
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);

			finish();
		}
	}
}
