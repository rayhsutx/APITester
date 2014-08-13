package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupLoginRequest extends KupRequest {

	@SerializedName("user-name")
	private String mUser;
	@SerializedName("password")
	private String mPassword;
	
	private transient String mAddress;
	private transient String mBucket;
	
	public KupLoginRequest(String address, String bucket, String user, String password)
	{
		this(null, address, bucket, user, password);
	}
	
	public KupLoginRequest(IKupResponseListener listener, String address, String bucket, String user, String password)
	{
		super(listener);
		
		mAddress = address;
		mBucket = bucket;
		mUser = user;
		mPassword = password;
	}

	public void submit() {
		if (!mAddress.endsWith("/"))
			mAddress += "/";
		
		requestAsync(getRequestAPIUrl(), getRequestAPIUrl(), 
				new KupSession("http://" + mAddress, "", mBucket, mUser), 
				getDeserializedClass());
	}
	
	protected void onResult(String api, KupResponseResult result)
	{
		if (result != null && result.isSuccessful())
		{
			KupLoginResult loginResult = (KupLoginResult) result;
			result.getSession().setUserId(loginResult.getUserId());
			result.getSession().setSessionKey(loginResult.getSessionKey());
		}
		
		super.onResult(api, result);
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.LOGIN;
	}

	@Override
	protected void setPostData() {
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupLoginResult.class;
	}
	
	public static class KupLoginResult extends KupResponseResult
	{
		@SerializedName("session-key")
		private String sessionKey;
		@SerializedName("user-id")
		private int mUserId;
		
		public String getSessionKey()
		{
			return sessionKey;
		}

		public int getUserId() {
			return mUserId;
		}
	}
}
