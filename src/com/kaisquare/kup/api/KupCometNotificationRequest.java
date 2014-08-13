package com.kaisquare.kup.api;

import com.google.gson.Gson;

public class KupCometNotificationRequest extends KupUserRequest {

	public KupCometNotificationRequest() {
		super();
	}

	public KupCometNotificationRequest(IKupResponseListener listener) {
		super(listener);
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.COMET_NOTIFICATION;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupCometNotificationResult.class;
	}

	public static class KupCometNotificationResult extends KupResponseResult
	{
		private String event;
		
		public KupEvent getEvent()
		{
			Gson gson = new Gson();
			try {
				return gson.fromJson(event, KupEvent.class);
			} catch (Exception e) {
				return null;
			}
		}
	}
}
