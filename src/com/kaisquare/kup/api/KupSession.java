package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

public class KupSession {

	@SerializedName("session-key")
	private String mSession;

	private transient int mUserId;
	private transient String mAddress;
	private transient String mBucket;
	private transient String mUser;
	private transient String mCookie;
	
	KupSession(String address, String session, String bucket, String user)
	{
		mAddress = address;
		mSession = session;
		mBucket = bucket;
		mUser = user;
	}
	
	void setSessionKey(String session)
	{
		mSession = session;
	}
	
	public String getAddress()
	{
		return mAddress;
	}
	
	public String getSessionKey()
	{
		return mSession;
	}
	
	public String getBucket()
	{
		return mBucket;
	}
	
	void setUserId(int id)
	{
		mUserId = id;
	}
	
	public int getUserId()
	{
		return mUserId;
	}
	
	public String getUser()
	{
		return mUser;
	}
	
	public void setCookie(String cookie)
	{
		mCookie = cookie;
	}
	
	public String getCookie()
	{
		return mCookie;
	}
}
