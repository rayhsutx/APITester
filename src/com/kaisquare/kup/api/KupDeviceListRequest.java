package com.kaisquare.kup.api;

import java.util.List;

public class KupDeviceListRequest extends KupUserRequest {
	
	public KupDeviceListRequest() {
		super();
	}

	public KupDeviceListRequest(IKupResponseListener listener) {
		super(listener);
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.USER_DEVICES;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupDeviceListResult.class;
	}
	
	public static class KupDeviceListResult extends KupResponseResult {
		public List<KupDevice> devices;
	}
}
