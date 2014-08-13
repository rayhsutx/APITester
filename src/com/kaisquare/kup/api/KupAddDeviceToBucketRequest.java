package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupAddDeviceToBucketRequest extends KupUserRequest {
	
	@SerializedName("registration-number")
	public String registrationNumber;
	@SerializedName("device-name")
	public String deviceName;
	@SerializedName("model-id")
	public String modelId;
	@SerializedName("device-key")
	public String key;
	@SerializedName("device-host")
	public String host;
	@SerializedName("device-port")
	public String port;
	@SerializedName("device-label")
	public String label;
	@SerializedName("device-login")
	public String login;
	@SerializedName("device-password")
	public String password;
	@SerializedName("device-address")
	public String address;
	@SerializedName("device-latitude")
	public String latitude;
	@SerializedName("device-longitude")
	public String longitude;
	@SerializedName("cloud-recording-enabled")
	public String cloudRecordingEnabled;

	@Override
	String getRequestAPIUrl() {
		return KupAPI.ADD_DEVICE_TO_BUCKET;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupAddDeviceToBucketResult.class;
	}
	
	public static class KupAddDeviceToBucketResult extends KupResponseResult
	{
		@SerializedName("id")
		public String platformDeviceId;
		@SerializedName("device-id")
		public String coreDeviceId;
	}

}
