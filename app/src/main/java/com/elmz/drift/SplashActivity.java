package com.elmz.drift;

	import android.app.Activity;
	import android.content.Intent;
	import android.os.Bundle;
	import android.os.Handler;

public class SplashActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		// TODO: preloading 'n such will go here; for now, adding time delay

		new Handler().postDelayed(new Runnable(){
			@Override
			public void run(){
				Intent intent = new Intent(SplashActivity.this, LoginConnectActivity.class);
				startActivity(intent);

				finish();
			}
		}, 3000);
	}
}