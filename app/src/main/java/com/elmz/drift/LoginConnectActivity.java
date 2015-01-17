package com.elmz.drift;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class LoginConnectActivity extends Activity{

	private EditText inpUsername;
	private EditText inpPassword;
	private Button btnLoginSubmit;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_connect);

		inpUsername = (EditText) findViewById(R.id.text_input_username);
		inpPassword = (EditText) findViewById(R.id.text_input_password);
		btnLoginSubmit = (Button) findViewById(R.id.btn_login_submit);

		// TODO: any kind of actual function

		btnLoginSubmit.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				Intent intent = new Intent(LoginConnectActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});
	}
}