package com.elmz.drift;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ChartView extends View{
	private int minimum;
	private int threshold;
	private int maximum;

	public ChartView(Context context){
		super(context);
		init();
	}

	public ChartView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public ChartView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		init();
	}

	public ChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init(){
		minimum = 0;
		maximum = 25;
		threshold = 10;
	}

	public int getMinimum(){
		return minimum;
	}

	public void setMinimum(int minimum){
		this.minimum = minimum;
		setThreshold(threshold);
	}

	public int getMaximum(){
		return maximum;
	}

	public void setMaximum(int maximum){
		this.maximum = maximum;
		setThreshold(threshold);
	}

	public int getThreshold(){
		return threshold;
	}

	public void setThreshold(int threshold){
		this.threshold = Math.min(Math.max(threshold, minimum), maximum);
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);

		float thr = canvas.getHeight()*(1f - (threshold-minimum)/(maximum-minimum));

		Paint pnt = new Paint();

		pnt.setColor(getResources().getColor(R.color.ok_pale));

		canvas.drawRect(0, thr, canvas.getWidth(), canvas.getHeight(), pnt);

		pnt.setColor(getResources().getColor(R.color.warning_pale));

		canvas.drawRect(0, 0, canvas.getWidth(), thr, pnt);
	}
}