package com.kaisquare.kup.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class KupDeviceModelsRequest extends KupUserRequest {

	@Override
	String getRequestAPIUrl() {
		return KupAPI.DEVICE_MODELS;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return null;
	}
	
	class KupDeviceModelsResult extends KupResponseResult
	{
		@SerializedName("model-list")
		List<KupModel> models;
	}

}
