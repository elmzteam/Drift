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
	import com.google.gson.JsonArray;
	import com.google.gson.JsonElement;
	import com.google.gson.JsonObject;

	import org.json.JSONArray;
	import org.json.JSONObject;

	import java.text.DateFormat;
	import java.text.FieldPosition;
	import java.text.ParsePosition;
	import java.text.SimpleDateFormat;
	import java.util.Calendar;
	import java.util.Date;

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

				try{
					JsonArray drivesJson = arg.getAsJsonArray();

					Drive[] drives = new Drive[drivesJson.size()];

					for(int i = 0; i < drivesJson.size(); i++){
						drives[i] = new Drive();

						JsonObject driveObj = drivesJson.get(i).getAsJsonObject();

						Log.d(getString(R.string.log_tag), driveObj.toString());

						drives[i].setFrom(driveObj.get("startLocation").getAsString());
						drives[i].setTo(driveObj.get("endLocation").getAsString());

						drives[i].setStart(new Date(Long.parseLong(driveObj.get("startTime").getAsString())));
						drives[i].setEnd(new Date(Long.parseLong(driveObj.get("endTime").getAsString())));

						drives[i].setScore(driveObj.get("avgFatigue").getAsInt());
					}

					Log.d(getString(R.string.log_tag), "setting adapter");

					adapter = new HistoryAdapter(drives);
					historyList.setAdapter(adapter);
					historyList.setVisibility(View.VISIBLE);
				} catch(Exception e){
					Log.e(getString(R.string.log_tag), "error :(");
				}
			}
		});

		arf.execute("POST", "getUserTrips", username, authToken);

		return view;
	}
}
