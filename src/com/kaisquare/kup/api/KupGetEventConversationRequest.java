package com.kaisquare.kup.api;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class KupGetEventConversationRequest extends KupUserRequest {
	
	@SerializedName("event-id")
	private String eventId;
	@SerializedName("up-to-date-time")
	private String baseTime;
	@SerializedName("num-of-messages")
	private int numOfMessages;

	private transient SimpleDateFormat mFormat;
	
	public KupGetEventConversationRequest(String eventId) {
		this(eventId, System.currentTimeMillis(), 10);
	}
	
	public KupGetEventConversationRequest(String eventId, long baseTime, int numOfMessages)
	{
		mFormat = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
		this.eventId = eventId;
		this.baseTime = mFormat.format(new Date(baseTime));
		this.numOfMessages = numOfMessages;
	}

	public KupGetEventConversationRequest(IKupResponseListener listener) {
		super(listener);
	}
	
	public void setBaseTime(long time)
	{
		baseTime = mFormat.format(new Date(time));
	}
	
	public void setNumOfMessages(int num)
	{
		numOfMessages = num;
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.GET_EVENT_CONVERSATION;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupEventConversationResult.class;
	}

	public static class KupEventConversationResult extends KupResponseResult
	{
		public KupEventConversation conversation;
	}
}
