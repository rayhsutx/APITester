package com.kaisquare.kup.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class KupEventListRequest extends KupUserRequest {
	
	@SerializedName("event-type")
	private String type;
	@SerializedName("skip")
	private int index = 0;
	@SerializedName("take")
	private int length = 30;

	public KupEventListRequest() {
		super();
	}

	public KupEventListRequest(IKupResponseListener listener) {
		super(listener);
	}
	
	public void setDataLimit(int index, int length)
	{
		this.index = index;
		this.length = length;
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.EVENTS;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupEventListResult.class;
	}

	public static class KupEventListResult extends KupResponseResult
	{
		@SerializedName("totalcount")
		public int totalCount;
		
		public List<KupEvent> events;
		
	}
}
