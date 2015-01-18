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
		public TextView scoreText;
		public TextView titleText;
		public TextView subtitleText;

		public ViewHolder(View v) {
			super(v);
			scoreText = (TextView) v.findViewById(R.id.history_item_score);
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
		holder.scoreText.setText(Integer.toString(data[ind].getScore()));
		DateFormat df = new SimpleDateFormat("MMM d, h:mm a");
		holder.titleText.setText(data[ind].getFrom() + " to " + data[ind].getTo());
		holder.subtitleText.setText(df.format(data[ind].getStart()) + " - " + df.format(data[ind].getEnd()));
	}

	@Override
	public int getItemCount() {
		return data.length;
	}
}