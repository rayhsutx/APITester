package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupPlaybackVideoRequest extends KupLiveVideoRequest {
	
	@SerializedName("from")
	private String from;
	@SerializedName("to")
	private String to;
	
	public KupPlaybackVideoRequest(IKupResponseListener listener,
			String deviceId, String channelId, String streamType, String from, String to) {
		super(listener, deviceId, channelId, streamType);
		
		setTimePeriod(from, to);
	}

	public KupPlaybackVideoRequest(String deviceId, String channelId,
			String streamType, String from, String to) {
		super(deviceId, channelId, streamType);
		
		setTimePeriod(from, to);
	}
	
	public void setTimePeriod(String from, String to)
	{
		this.from = from;
		this.to = to;
	}
	
	@Override
	String getRequestAPIUrl() {
		return KupAPI.PLAYBACK_VIDEO;
	}
}
