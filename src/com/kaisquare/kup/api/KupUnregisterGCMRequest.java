package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupUnregisterGCMRequest extends KupUserRequest {
	
	@SerializedName("device-token")
	private String deviceToken;

	public KupUnregisterGCMRequest(String token) {
		deviceToken = token;
	}

	public KupUnregisterGCMRequest(IKupResponseListener listener, String token) {
		super(listener);
		deviceToken = token;
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.UNREGISTER_GCM_DEVICE;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupResponseResult.class;
	}

}
