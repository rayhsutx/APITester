package com.kaisquare.kup.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KupLocation {
	public double latitude;
	public double longitude;
	public String time;
	
	public Date getTime()
	{
		SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmmss");
		try {
			return format.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
