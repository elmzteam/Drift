package com.elmz.drift;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

public class StatusFragment extends Fragment {
	private DrowsinessView drowsinessView;
	private TextView textBlinkRate;
	private TextView textBlinkLength;
	private double blinkRate;
	private double blinkLength;

	private Thread updater;

	public StatusFragment(){
		super();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.status, container, false);

		SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

		String username = sp.getString("username", "");
		String authToken = sp.getString("authToken", "");

		drowsinessView = (DrowsinessView) view.findViewById(R.id.drowsiness_view);
		textBlinkLength = (TextView) view.findViewById(R.id.text_blink_length);
		textBlinkRate = (TextView) view.findViewById(R.id.text_blink_rate);

		blinkRate = 15;
		blinkLength = .2;
		drowsinessView.setValue(50);

		final Handler handler = new Handler();

		updater = new Thread(new Runnable(){
			@Override
			public void run(){
				while(true){
					try{
						Thread.sleep(100);
						handler.post(new Runnable(){
							@Override
							public void run(){
								Log.d(getString(R.string.log_tag), "updating");
								updateFatigueIndex(drowsinessView.getValue()+(int)Math.round(Math.random()*1.2-.6));
								updateBlinkRate(Math.random()/10-.05);
								updateBlinkLength(Math.random()/25-.02);
							}
						});
					} catch(Exception e){

					}
				}
			}
		});
		updater.start();

		return view;
	}

	private void updateFatigueIndex(int val){
		drowsinessView.setValue(drowsinessView.getValue() + val);
	}

	private void updateBlinkRate(double rate){
		blinkRate += rate;
		textBlinkRate.setText(new DecimalFormat("#0.0").format(blinkRate));
	}

	private void updateBlinkLength(double length){
		blinkLength += length;
		textBlinkLength.setText(new DecimalFormat("#0.00").format(blinkLength));
	}

	@Override
	public void onStop(){
		updater.stop();
	}
}
