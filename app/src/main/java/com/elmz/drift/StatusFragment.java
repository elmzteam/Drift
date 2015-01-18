package com.elmz.drift;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elmz.drift.openbci.AlphaDetector;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

public class StatusFragment extends Fragment{
	public static StatusFragment newInstance(Listener l){
		return new StatusFragment(l);
	}

	private DrowsinessView drowsinessView;
	private TextView textBlinkRate;
	private TextView textBlinkLength;
    private long blinkTimeRange = 15000;
    private Queue<Long> blinks = new LinkedList<Long>();
    private boolean blinkLengthBufferFilled = false;
    private int indexCycler = 0;
    private double[] blinkLengthBuffer = {0,0,0,0,0};
	private double currentBlinkRate;
	private double currentBlinkLength;
	private ChartView alphaChart;
	private TextView textAlpha;
    private Listener mListener;

    public interface Listener {
        public void startStreaming();
    }

	public StatusFragment(Listener l) {
        super();
        mListener = l;
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
    public void onStart() {
        super.onStart();
        mListener.startStreaming();
    }

    public void onGetUpdate(boolean getUpdate, int dataformat, Object data) {
        if (getUpdate) {
            switch(dataformat) {
                case 1:
                    blinks.add(System.currentTimeMillis());
                    updateBlinkRate();
                    break;
                case 2:
                    double blink = (double)data;
                    updateBlinkLength(blink);
                    break;
                case 3:
                    AlphaDetector.DetectionData_FreqDomain[] alph = (AlphaDetector.DetectionData_FreqDomain[])data;
                    double alphampsum = 0;
                    for (AlphaDetector.DetectionData_FreqDomain ddfd : alph) alphampsum += ddfd.inband_vs_guard_dB;
                    updateAlpha(alphampsum/alph.length);
                    break;
            }
        }
    }

	private void updateFatigueIndex(int val){
		drowsinessView.setValue(drowsinessView.getValue() + val);
	}

	private void updateBlinkRate(){
        while (System.currentTimeMillis() - blinks.peek() > blinkTimeRange) {
            blinks.poll();
        }
		currentBlinkRate = blinks.size() / (blinkTimeRange / 1000d);
		textBlinkRate.setText(new DecimalFormat("#0.0").format(currentBlinkRate));
	}

	private void updateBlinkLength(double length){
        blinkLengthBuffer[indexCycler] = length;
        indexCycler++;
        if (blinkLengthBufferFilled) {
            double sum = 0;
            for (double d : blinkLengthBuffer) sum += d;
            currentBlinkLength = sum / blinkLengthBuffer.length;
            textBlinkLength.setText(new DecimalFormat("#0.00").format(currentBlinkLength));
        } else {
            if (indexCycler == 4) blinkLengthBufferFilled = true;
        }
	}

	private void updateAlpha(double ampl){
		alphaChart.pushData(ampl);
		textAlpha.setText(new DecimalFormat("#0").format(ampl));
	}
}
