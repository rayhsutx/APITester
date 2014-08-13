package com.kaisquare.kup.api;

public class KupModel {

	public String id;
	public int modelId;
	public String name;
	public int channels;
	public String capabilities;
	
	public boolean hasVideo()
	{
		return capabilities.contains("video");
	}
	
	public boolean hasGPS()
	{
		return capabilities.contains("gps");
	}
}
