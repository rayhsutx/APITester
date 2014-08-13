package com.kaisquare.kup.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class KupLiveVideoRequest extends KupUserRequest {

	@SerializedName("device-id")
	private String deviceId;
	@SerializedName("channel-id")
	private String channelId;
	@SerializedName("stream-type")
	private String streamType;
	
	public KupLiveVideoRequest(String deviceId, String channelId, String streamType) {
		super();
		setValue(deviceId, channelId, streamType);
	}

	public KupLiveVideoRequest(IKupResponseListener listener, String deviceId, String channelId, String streamType) {
		super(listener);
		setValue(deviceId, channelId, streamType);
	}
	
	private void setValue(String deviceId, String channelId, String streamType)
	{
		this.deviceId = deviceId;
		this.channelId = channelId;
		this.streamType = streamType;
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.LIVE_VIDEO;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupLiveVideoResult.class;
	}

	public static class KupLiveVideoResult extends KupResponseResult
	{
		@SerializedName("streaming-session-key")
		public String streamSessionKey;
		public List<String> url;
	}
}
