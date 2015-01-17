package com.elmz.drift.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elmz.drift.R;
import com.elmz.drift.items.Drive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
			dateText = (TextView) v.findViewById(R.id.history_item_date);
			titleText = (TextView) v.findViewById(R.id.history_item_title);
			subtitleText = (TextView) v.findViewById(R.id.history_item_subtitle);
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
		holder.monthText.setText(months[data[ind].getStart().getMonth()]);
		holder.dateText.setText(Integer.toString(data[ind].getStart().getDate()));
		DateFormat df = new SimpleDateFormat("MMM d, h:mm a");
		holder.titleText.setText(data[ind].getFrom() + " to " + data[ind].getTo());
		holder.subtitleText.setText(df.format(data[ind].getStart()) + " - " + df.format(data[ind].getEnd()));
	}

	@Override
	public int getItemCount() {
		return data.length;
	}
}