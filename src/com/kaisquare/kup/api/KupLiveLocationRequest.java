package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupLiveLocationRequest extends KupUserRequest {
	
	@SerializedName("device-id")
	private String deviceId;

	public KupLiveLocationRequest(String deviceId) {
		this(null, deviceId);
	}

	public KupLiveLocationRequest(IKupResponseListener listener, String deviceId) {
		super(listener);
		
		this.deviceId = deviceId;
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.LIVE_LOCATION;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupLiveLocationResult.class;
	}
	
	public static class KupLiveLocationResult extends KupResponseResult
	{
		public KupLocation location;
	}

}
