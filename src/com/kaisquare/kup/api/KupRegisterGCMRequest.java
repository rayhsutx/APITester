package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupRegisterGCMRequest extends KupUserRequest {
	
	@SerializedName("device-token")
	private String deviceToken;

	public KupRegisterGCMRequest(String token) {
		deviceToken = token;
	}

	public KupRegisterGCMRequest(IKupResponseListener listener, String token) {
		super(listener);
		deviceToken = token;
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.REGISTER_GCM_DEVICE;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupResponseResult.class;
	}

}
