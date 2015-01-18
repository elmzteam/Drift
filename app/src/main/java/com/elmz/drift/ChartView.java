package com.elmz.drift;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import java.util.Iterator;
import java.util.LinkedList;

public class ChartView extends View{
	private double minimum;
	private double threshold;
	private double maximum;
	private Paint pnt;
	private LinkedList<Double> data;
	private float dpConversion;

	public static final int MAXIMUM_DATA_STORAGE = 200;

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
		pnt = new Paint();
		data = new LinkedList<Double>();
		dpConversion = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
	}

	public double getMinimum(){
		return minimum;
	}

	public void setMinimum(double minimum){
		this.minimum = minimum;
		setThreshold(threshold);
	}

	public double getMaximum(){
		return maximum;
	}

	public void setMaximum(double maximum){
		this.maximum = maximum;
		setThreshold(threshold);
	}

	public double getThreshold(){
		return threshold;
	}

	public void setThreshold(double threshold){
		this.threshold = Math.min(Math.max(threshold, minimum), maximum);
	}

	public void pushData(double value){
		value = Math.min(Math.max(value, minimum), maximum);
		data.push(value);

		if(data.size() > MAXIMUM_DATA_STORAGE){
			data.removeLast();
		}

		invalidate();
	}

	private float getScaledY(Canvas canvas, double y){
		return (float)(canvas.getHeight()*(1 - (y-minimum)/(maximum-minimum)));
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);

		float thr = getScaledY(canvas, threshold);

		pnt.setColor(getResources().getColor(R.color.ok_pale));
		pnt.setStrokeWidth(0);

		canvas.drawRect(0, thr, canvas.getWidth(), canvas.getHeight(), pnt);

		pnt.setColor(getResources().getColor(R.color.warning_pale));

		canvas.drawRect(0, 0, canvas.getWidth(), thr, pnt);

		pnt.setColor(getResources().getColor(R.color.accent));
		pnt.setStrokeWidth(2 * dpConversion);

		Iterator<Double> it = data.iterator();

		if(!it.hasNext()){
			return;
		}

		float dx = (float)getWidth()/MAXIMUM_DATA_STORAGE;
		float x = getWidth() - 8 * dpConversion;
		double last = it.next();

		while(it.hasNext()){
			double next = it.next();
			canvas.drawLine(x, getScaledY(canvas, last), x-dx, getScaledY(canvas, next), pnt);
			last = next;
			x -= dx;
		}

		canvas.drawCircle(getWidth()-8*dpConversion, getScaledY(canvas, data.getFirst()), 4*dpConversion, pnt);
	}
}