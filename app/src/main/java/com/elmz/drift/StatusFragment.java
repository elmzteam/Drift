package com.elmz.drift;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.elmz.drift.adapters.HistoryAdapter;
import com.elmz.drift.items.Drive;
import com.google.gson.JsonElement;

import java.util.Calendar;

public class StatusFragment extends Fragment{
	public static StatusFragment newInstance(){
		return new StatusFragment();
	}

	private DrowsinessView drowsinessView;

	public StatusFragment(){
		super();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.status, container, false);

		SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

		String username = sp.getString("username", "");
		String authToken = sp.getString("authToken", "");

		drowsinessView = (DrowsinessView) view.findViewById(R.id.drowsiness_view);

		drowsinessView.setValue(56);

		return view;
	}
}
