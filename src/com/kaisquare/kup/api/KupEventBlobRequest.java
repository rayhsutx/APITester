package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupEventBlobRequest extends KupUserRequest {
	
	@SerializedName("event-id")
	public String eventId;
	@SerializedName("blob-id")
	public String blobId;

	public KupEventBlobRequest(String eventId, String blobId, IKupRawDataReceivedListener listener) {
		this(listener);
		
		this.eventId = eventId;
		this.blobId = blobId;
	}

	public KupEventBlobRequest(IKupRawDataReceivedListener listener) {
		super(null);
		
		setRawDataReceivedListener(listener);
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.GET_EVENT_BLOB;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return null;
	}

	
}
