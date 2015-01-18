package com.elmz.drift;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

public class DrowsinessView extends TextView{
	private int minimum;
	private int maximum;
	private int value;

	public DrowsinessView(Context context){
		super(context);
		init();
	}

	public DrowsinessView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public DrowsinessView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		init();
	}

	public DrowsinessView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init(){
		ViewOutlineProvider vop = new ViewOutlineProvider(){
			@Override
			public void getOutline(View view, Outline outline){
				int size = getResources().getDimensionPixelSize(R.dimen.drowsiness_view_size);
				outline.setOval(0, 0, size, size);
			}
		};
		setOutlineProvider(vop);
		setClipToOutline(true);
		minimum = 0;
		maximum = 100;
		value = 0;
	}

	public int getMinimum(){
		return minimum;
	}

	public void setMinimum(int minimum){
		this.minimum = minimum;
		setValue(value);
	}

	public int getMaximum(){
		return maximum;
	}

	public void setMaximum(int maximum){
		this.maximum = maximum;
		setValue(value);
	}

	public int getValue(){
		return value;
	}

	public void setValue(int value){
		this.value = value;
		value = Math.min(Math.max(value, minimum), maximum);
		setText(Integer.toString(value));
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas){
		int size = getResources().getDimensionPixelSize(R.dimen.drowsiness_view_size);
		int r = getResources().getDimensionPixelSize(R.dimen.drowsiness_ring_inner_radius);
		float theta = (float)(360f/(maximum-minimum)*(value-minimum));

		Paint pnt = new Paint();

		pnt.setColor(getResources().getColor(R.color.ok));

		canvas.drawOval(0, 0, size, size, pnt);

		pnt.setColor(getResources().getColor(R.color.warning));

		canvas.drawArc(0, 0, size, size, 0, theta, true, pnt);

		pnt.setColor(getResources().getColor(R.color.white));

		canvas.drawOval(size/2-r, size/2-r, size/2+r, size/2+r, pnt);

		super.onDraw(canvas);
	}
}
