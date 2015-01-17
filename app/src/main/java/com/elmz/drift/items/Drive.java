package com.elmz.drift.items;

import java.util.Date;

public class Drive{
	private String from;
	private String to;
	private Date start;
	private Date end;

	public String getFrom(){
		return from;
	}

	public void setFrom(String from){
		this.from = from;
	}

	public String getTo(){
		return to;
	}

	public void setTo(String to){
		this.to = to;
	}

	public Date getStart(){
		return start;
	}

	public void setStart(Date start){
		this.start = start;
	}

	public Date getEnd(){
		return end;
	}

	public void setEnd(Date end){
		this.end = end;
	}
}
