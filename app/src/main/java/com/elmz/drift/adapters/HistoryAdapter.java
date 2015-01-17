package com.elmz.drift.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elmz.drift.R;
import com.elmz.drift.items.Drive;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
	private Drive[] data;
	private String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView monthText;
		public TextView dateText;
		public TextView titleText;
		public TextView subtitleText;

		public ViewHolder(View v) {
			super(v);
			monthText = (TextView) v.findViewById(R.id.history_item_month);
			dateText = (TextView) v.findViewById(R.id.history_item_month);
			titleText = (TextView) v.findViewById(R.id.history_item_month);
			subtitleText = (TextView) v.findViewById(R.id.history_item_month);
		}
	}

	public HistoryAdapter(Drive[] dat) {
		data = dat;
	}

	@Override
	public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int ind) {
		holder.monthText.setText(months[data[ind].start.getMonth()]);
		holder.dateText.setText(Integer.toString(data[ind].start.getDate()));
		holder.titleText.setText(data[ind].from + " to " + data[ind].to);
		holder.subtitleText.setText(data[ind].start.toString() + " - " + data[ind].end.toString());
	}

	@Override
	public int getItemCount() {
		return data.length;
	}
}