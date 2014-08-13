package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupNodeRegistrationRequest extends KupRequest {
	
	@SerializedName("license-number")
	public String licenseNumber;
	@SerializedName("registration-number")
	public String registrationNumber;
	@SerializedName("device-name")
	public String deviceName;
	@SerializedName("device-address")
	public String address;
	@SerializedName("device-latitude")
	public String latitude;
	@SerializedName("device-longitude")
	public String longitude;
	
	private transient String mHost;

	public KupNodeRegistrationRequest(String host)
	{
		super();
		setNodeRequest(true);
	}
	
	public KupResponseResult submit()
	{
		return request(new KupSession("http://" + mHost, "", "", ""));
	}
	
	@Override
	protected void setPostData() {
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.REGISTER_NODE;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupNodeRegistrationResult.class;
	}

	public static class KupNodeRegistrationResult extends KupResponseResult
	{
		
	}
}
