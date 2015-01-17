package com.elmz.drift;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChartFragment extends Fragment{
	public ChartFragment(){
		super();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view =  inflater.inflate(R.layout.chart, container, false);
		return view;
	}
}
