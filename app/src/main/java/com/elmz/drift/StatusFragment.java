package com.elmz.drift;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elmz.drift.openbci.AlphaDetector;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

public class StatusFragment extends Fragment{

	private DrowsinessView drowsinessView;
	private TextView textBlinkRate;
	private TextView textBlinkLength;
	private long blinkTimeRange = 7231;
	private Queue<Long> blinks = new LinkedList<Long>();
	private boolean blinkLengthBufferFilled = false;
	private int indexCycler = 0;
	private double[] blinkLengthBuffer = new double[5];
	private double currentBlinkRate;
	private double currentBlinkLength;
    private double currentAlpha;
	private ChartView alphaChart;
	private TextView textAlpha;
	private Listener mListener;

	public interface Listener{
		public void startStreaming();
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

	@Override
	public void onStart(){
		super.onStart();
		mListener.startStreaming();
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
					+ " must implement StatusFragment.Listener");
		}
	}

	public void onGetUpdate(boolean getUpdate, int dataformat, Object data){
		if(getUpdate){
			switch(dataformat){
				case 1:
					blinks.add(System.currentTimeMillis());
					updateBlinkRate();
					break;
				case 2:
					double blink = (double) data;
					updateBlinkLength(blink);
					break;
				case 3:
					AlphaDetector.DetectionData_FreqDomain[] alph = (AlphaDetector.DetectionData_FreqDomain[]) data;
					double alphampsum = 0;
                    boolean isalpha = true;
					for(AlphaDetector.DetectionData_FreqDomain ddfd : alph) {
                        alphampsum += ddfd.inband_vs_guard_dB;
                        isalpha = isalpha && ddfd.isDetected;
                    }
                    if (isalpha) {
                        SoundHelper.alarm(getActivity());
                    }
					updateAlpha(alphampsum / alph.length);
					break;
			}
            updateFatigueIndex();
		}
	}

	private void updateFatigueIndex() {
        int val = (int)Math.floor((1/(1 + Math.exp(-7 *((5 + currentAlpha)/15.0 - .5)))*0.60 + (currentBlinkLength >= .25 ? 1 :
                0) + 0.1 + 1/(1 + Math.exp(-7 *((1 - 60/(2.0*currentBlinkRate)) - .5)))*0.3)*100);
        Log.d("Fatigue", "" + val);
		drowsinessView.setValue(val);
	}

	private void updateBlinkRate(){
		while(System.currentTimeMillis() - blinks.peek() > blinkTimeRange){
			blinks.poll();
		}
		currentBlinkRate = blinks.size() / (blinkTimeRange / 1000d) * 60;
		textBlinkRate.setText(new DecimalFormat("#0.0").format(currentBlinkRate));
	}

	private void updateBlinkLength(double length){
		blinkLengthBuffer[indexCycler] = length;
		indexCycler++;
		if(blinkLengthBufferFilled){
			double sum = 0;
			for(double d : blinkLengthBuffer) sum += d;
			currentBlinkLength = sum / blinkLengthBuffer.length;
			textBlinkLength.setText(new DecimalFormat("#0.00").format(currentBlinkLength));
		} else {
			if(indexCycler == blinkLengthBuffer.length-1) blinkLengthBufferFilled = true;
		}
		if(indexCycler >= blinkLengthBuffer.length){
			indexCycler -= blinkLengthBuffer.length;
		}
	}

	private void updateAlpha(double ampl){
        currentAlpha = ampl;
		alphaChart.pushData(currentAlpha);
		textAlpha.setText(new DecimalFormat("#0").format(currentAlpha));
	}
}
