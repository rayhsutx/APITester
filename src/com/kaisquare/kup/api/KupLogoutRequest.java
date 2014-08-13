package com.kaisquare.kup.api;

public class KupLogoutRequest extends KupUserRequest {

	public KupLogoutRequest() {
	}

	public KupLogoutRequest(IKupResponseListener listener) {
		super(listener);
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.LOGOUT;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupResponseResult.class;
	}

}
