package com.kaisquare.kup.api;

public class KupEvent {
	public String id;
	public String data;
	public String type;
	public String time;
	public String deviceId;
	public String patchEventVideoURL;
	public KupDevice device;
	public int messageCount;
	
	public static class RMSEvent
	{
		public int eventType; //RMSMessage.SMFEventType
		public String mediaType; //video, image
		public int mediaFormat;
		public String timeFrom;
		public String timeTo;
		public String mediaUrl;
		public String snapshot;
	}
}
