package com.kaisquare.kup.api;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.kaisquare.gson.GsonBuilder;

public class KupMessage {
	public String eventId;
	public int senderId;
	public String senderName;
	public String content;
	public MessageTimestamp timestamp;
	
	private transient KupMessageContent sContent;
	
	public void createMessageContent(int type, String mimeType, String name, byte[] data, long time)
	{
		Object object = null;
		switch (type)
		{
		case KupMessageContent.TYPE_TEXT:
		case KupMessageContent.TYPE_IMAGE:
			object = new String(data);
			break;
		}
		sContent = new KupMessageContent(type, mimeType, name, object, time);
	}
	
	public void setTimestamp(long milliseconds)
	{
		timestamp = new MessageTimestamp();
		timestamp.milliseconds = milliseconds;
	}
	
	public void setMessageContent(KupMessageContent content)
	{
		sContent = content;
	}
	
	public KupMessageContent getMessageContent()
	{
		if (sContent == null)
		{
			if (!"".equals(content))
			{
				Gson gson = GsonBuilder.create();
				try {
					sContent = gson.fromJson(content, KupMessageContent.class);
				} catch (Exception e) {
					sContent = new KupMessageContent(KupMessageContent.TYPE_TEXT, "", "", content, 0);
				}
			}
		}
		
		return sContent;
	}
	
	public class MessageTimestamp
	{
		@SerializedName("iMillis")
		public long milliseconds;
		@SerializedName("iChronology")
		public Chronology chronology;
		
		public class Chronology {
			@SerializedName("iBase")
			public Base base;
			
			public class Base
			{
				@SerializedName("iMinDaysInFirstWeek")
				public int minDaysInFirstWeeky;
			}
		}
	}
}
