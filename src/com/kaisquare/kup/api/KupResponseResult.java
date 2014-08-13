package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;

/**
 * This the basic object for the response from server and it only holds the result status and session,
 * to inherit this object to add more data to be serialized from server response data.
 * 
 * This object provides two shortcut {@link isSuccessful} and {@link isError} to check the response status
 */
public class KupResponseResult {
	
	@SerializedName("result")
	public String result;
	
	@SerializedName("reason")
	public String reason;
	
	private transient KupSession mSession;
	
	public boolean isSuccessful()
	{
		return "ok".equals(result);
	}
	
	public boolean isError()
	{
		return "error".equals(result);
	}
	
	public void setSession(KupSession session)
	{
		mSession = session;
	}
	
	public KupSession getSession()
	{
		return mSession;
	}
}
