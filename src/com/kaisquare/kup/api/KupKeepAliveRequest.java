package com.kaisquare.kup.api;

public class KupKeepAliveRequest extends KupUserRequest {

	public KupKeepAliveRequest() {
		super();
	}

	public KupKeepAliveRequest(IKupResponseListener listener) {
		super(listener);
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.KEEP_SESSION_ALIVE;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupResponseResult.class;
	}

}
