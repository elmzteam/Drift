package com.elmz.drift;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonElement;

public class LoginFragment extends Fragment
{
	private EditText inpUsername;
	private EditText inpPassword;
	private Button btnLoginSubmit;
	private ProgressBar loginSpinner;
	private TextView loginStatus;
	private ImageView loginDone;
	private ImageView connectDone;
	private FrameLayout noDevice;
	private LinearLayout foundDevice;
	private TextView deviceId;
	private Listener mListener;

	private boolean loginOk = false;
	private boolean deviceOk = false;

	private SharedPreferences sp;

	public interface Listener {
		public void home();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_connect, container, false);

		sp = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		inpUsername = (EditText) view.findViewById(R.id.text_input_username);
		inpPassword = (EditText) view.findViewById(R.id.text_input_password);
		btnLoginSubmit = (Button) view.findViewById(R.id.btn_login_submit);
		loginSpinner = (ProgressBar) view.findViewById(R.id.prog_login_spinner);
		loginStatus = (TextView) view.findViewById(R.id.text_login_status);
		loginDone = (ImageView) view.findViewById(R.id.ic_done_login);
		connectDone = (ImageView) view.findViewById(R.id.ic_done_connect);
		noDevice = (FrameLayout) view.findViewById(R.id.message_no_device);
		foundDevice = (LinearLayout) view.findViewById(R.id.message_device_found);
		deviceId = (TextView) view.findViewById(R.id.text_device_id);

		btnLoginSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				inpUsername.setEnabled(false);
				inpPassword.setEnabled(false);
				btnLoginSubmit.setEnabled(false);
				btnLoginSubmit.setVisibility(View.GONE);
				loginSpinner.setVisibility(View.VISIBLE);
				loginStatus.setText("Authorizing...");

				final String username = inpUsername.getText().toString();
				final String password = inpPassword.getText().toString();

				APIRequestFragment arf = new APIRequestFragment(new ICallback() {
					@Override
					public void callback(JsonElement tok) {
						if(tok.isJsonNull() || tok.getAsString().length() == 0){
							Log.d(getString(R.string.log_tag), "Login rejected");
							loginStatus.setText("Invalid username or password.");

							inpUsername.setEnabled(true);
							inpPassword.setEnabled(true);
							btnLoginSubmit.setEnabled(true);
							btnLoginSubmit.setVisibility(View.VISIBLE);
							loginSpinner.setVisibility(View.GONE);
						} else {
							Log.d(getString(R.string.log_tag), "Login accepted");
							SharedPreferences.Editor editor = sp.edit();
							editor.putString("authToken", tok.getAsString());
							editor.putString("username", username);
							editor.commit();
							onLogin();
						}
					}
				});
				arf.execute("POST", "authUser", username, password);

			}
		});

		if (sp.getString("authToken", null) != null && sp.getString("username", null) != null) {
			loginStatus.setText("Checking stored authorization...");
			inpUsername.setEnabled(false);
			inpPassword.setEnabled(false);
			btnLoginSubmit.setEnabled(false);
			btnLoginSubmit.setVisibility(View.GONE);
			loginSpinner.setVisibility(View.VISIBLE);
			inpUsername.setText(sp.getString("username", ""));

			APIRequestFragment arf = new APIRequestFragment(new ICallback() {
				@Override
				public void callback(JsonElement arg) {
					String status;
					if (!arg.isJsonNull()) {
						status = arg.getAsString();
						Log.d(getString(R.string.log_tag), "Auth validation: " + status);
						if (status.equals("true")) {
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

		
		// TODO: remove this

		view.findViewById(R.id.message_no_device).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				onDevice(true);
			}
		});

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mListener = (Listener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
				+ " must implement OnFragmentInteractionListener");
		}
	}

	void onLogin(){
		loginStatus.setText("Authorized.");

		loginDone.setVisibility(View.VISIBLE);
		loginStatus.requestFocus();
		loginOk = true;
		inpPassword.setText("");
		loginSpinner.setVisibility(View.GONE);
		checkForCompletion();
	}

	void onDevice(boolean enabled) {
		if (enabled) {
			connectDone.setVisibility(View.VISIBLE);
			noDevice.setVisibility(View.GONE);
			deviceId.setText("OpenBCI connected");
			foundDevice.setVisibility(View.VISIBLE);
			deviceOk = true;
			checkForCompletion();
		} else {
			connectDone.setVisibility(View.GONE);
			noDevice.setVisibility(View.VISIBLE);
			foundDevice.setVisibility(View.GONE);
			deviceOk = false;
		}
	}

	private void checkForCompletion(){
		if(loginOk && deviceOk){
			Log.d(getString(R.string.log_tag), "Passing to MainActivity");
			mListener.home();
		}
	}
}