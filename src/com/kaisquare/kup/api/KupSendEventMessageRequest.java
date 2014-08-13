package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupSendEventMessageRequest extends KupUserRequest {
	
	@SerializedName("event-id")
	private String eventId;
	@SerializedName("content")
	private String message;

	public KupSendEventMessageRequest(IKupResponseListener listener, String eventId, String message) {
		super(listener);
		
		this.eventId = eventId;
		this.message = message;
	}

	public KupSendEventMessageRequest(IKupResponseListener listener) {
		super(listener);
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.SEND_EVENT_MESSAGE;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupResponseResult.class;
	}

}
