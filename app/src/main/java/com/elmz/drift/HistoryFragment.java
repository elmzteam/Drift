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

public class HistoryFragment extends Fragment {
	public RecyclerView historyList;
	public ProgressBar historySpinner;
	public LinearLayoutManager llm;
	public HistoryAdapter adapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.history_list, container, false);

		historyList = (RecyclerView) view.findViewById(R.id.history_list);
		historySpinner = (ProgressBar) view.findViewById(R.id.prog_history_spinner);

		historyList.setHasFixedSize(true);

		llm = new LinearLayoutManager(getActivity().getApplicationContext());
		historyList.setLayoutManager(llm);

		SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

		String username = sp.getString("username", "");
		String authToken = sp.getString("authToken", "");

		APIRequestFragment arf = new APIRequestFragment(new ICallback(){
			@Override
			public void callback(JsonElement arg){
				historySpinner.setVisibility(View.INVISIBLE);

				// TODO: real data
				Drive[] drives = new Drive[20];

				String[] locations = {"Herndon, VA", "Alexandria, VA", "Philadelphia, PA", "New York, NY", "Wilmington, DE", "Baltimore, MA", "Orlando, FL", "Reston, VA", "Denver, CO", "San Diego, CA", "San Francisco, CA", "Salt Lake City, UT", "Las Vegas, NV", "Dallas, TX", "Houston, TX"};

				for(int i = 0; i < drives.length; i++){
					drives[i] = new Drive();
					drives[i].setFrom(locations[(int)Math.floor(Math.random() * locations.length)]);
					drives[i].setTo(locations[(int) Math.floor(Math.random() * locations.length)]);

					Calendar cal = Calendar.getInstance();

					cal.add(Calendar.HOUR, (int)(-Math.floor(Math.random()*10000)));

					drives[i].setStart(cal.getTime());

					cal.add(Calendar.MINUTE, (int)(Math.random() * 300));

					drives[i].setEnd(cal.getTime());
				}

				Log.d(getString(R.string.log_tag), "setting adapter");

				adapter = new HistoryAdapter(drives);
				historyList.setAdapter(adapter);
				historyList.setVisibility(View.VISIBLE);
			}
		});

		arf.execute("POST", "getUserTrips", username, authToken);

		return view;
	}
}
