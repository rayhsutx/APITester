package com.kaisquare.kup.api;

import com.google.gson.annotations.SerializedName;
import com.kaisquare.kaisync.utils.AppLogger;

/**
 * This is the abstract based request class for the logged-in user
 * To inherits this class that doesn't need to handle user session required request
 */
public abstract class KupUserRequest extends KupRequest {

	@SerializedName("session-key")
	protected String sessionKey;
	
	public KupUserRequest()
	{
		super();
	}

	public KupUserRequest(IKupResponseListener listener) {
		super(listener);
	}
	
	/** 
	 * NOTE: DO NOT override this method without calling super.setPostData
	 * or user session will be invalid 
	 */
	@Override
	protected void setPostData() {
		KupSession session = getSession();
		if (session == null)
			AppLogger.e(this, "KUP session is null");
		else
			sessionKey = getSession().getSessionKey();
	}
}
