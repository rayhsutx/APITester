package com.kaisquare.kup.api;

public class KupDevice {

	public String id;
	public String name;
	public String deviceId;
	public KupModel model;
	public KupNode node;
	public String deviceKey;
	public String host;
	public String port;
	public String login;
	public String password;
	public String address;
	public double latitude;
	public double longitude;
	public boolean cloudRecordingEnabled;
	public String bucket;

//	public KupBucket bucket;
	
	private String getAvailableStreamType()
	{
		if (model != null && model.capabilities != null)
		{
			if (model.capabilities.contains(KupConstants.CAP_VIDEO_H264))
				return KupConstants.RTSP_H264;
			else if (model.capabilities.contains(KupConstants.CAP_VIDEO_MJPEG))
				return KupConstants.HTTP_MJPEG;
			else
				return "";
		}
		else
			return KupConstants.CAP_VIDEO_MJPEG;
	}
	
	public KupResponseResult getLiveVideoUrl(int channel)
	{
		return getLiveVideoUrl(channel, getAvailableStreamType());
	}
	
	public KupResponseResult getLiveVideoUrl(int channel, String streamType)
	{
		return KupAPI.send(new KupLiveVideoRequest(this.deviceId, Integer.toString(channel), streamType));
	}
	
	public static int getVideoFormat(String url)
	{
		return url.startsWith("rtsp") ? 6 : 5;
	}
}
