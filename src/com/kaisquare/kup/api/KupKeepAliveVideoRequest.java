package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupKeepAliveVideoRequest extends KupUserRequest {
	
	@SerializedName("streaming-session-key")
	private String streamingSessionKey;

	public KupKeepAliveVideoRequest(String sessionKey) {
		this(sessionKey, null);
	}

	public KupKeepAliveVideoRequest(String sessionKey, IKupResponseListener listener) {
		super(listener);
		setStreamingSessionKey(sessionKey);
	}
	
	public void setStreamingSessionKey(String sessionKey)
	{
		streamingSessionKey = sessionKey;
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.KEEP_ALIVE_VIDEO;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupResponseResult.class;
	}
}
