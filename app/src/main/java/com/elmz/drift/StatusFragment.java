package com.elmz.drift;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

public class StatusFragment extends Fragment{
	public static StatusFragment newInstance(){
		return new StatusFragment();
	}

	private DrowsinessView drowsinessView;
	private TextView textBlinkRate;
	private TextView textBlinkLength;
	private double blinkRate;
	private double blinkLength;
	private ChartView alphaChart;
	private TextView textAlpha;

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
		alphaChart = (ChartView) view.findViewById(R.id.chart_alpha);
		textAlpha = (TextView) view.findViewById(R.id.text_current_alpha);

		drowsinessView.setValue(50);

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

	private void updateAlpha(double ampl){
		alphaChart.pushData(ampl);
		double val = (ampl - alphaChart.getMinimum())/(alphaChart.getMaximum() - alphaChart.getMinimum());
		textAlpha.setText(Integer.toString((int)(val*100)));
	}
}
