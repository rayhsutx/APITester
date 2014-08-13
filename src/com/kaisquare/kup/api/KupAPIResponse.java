package com.kaisquare.kup.api;

/**
 * The result of the API request
 * @param <T> result data which is converted by API request from JSON data
 */
public class KupAPIResponse<T> {
	
	private int mStatus;
	private String mCookie;
	private T mData;
	private String mError;

	/**
	 * Constructor of API response
	 * @param status the HTTP status of the request
	 * @param cookie the server response cookie (which is from Set-Cookie header)
	 * @param data result object
	 * @param error error message if status is -1
	 */
	public KupAPIResponse(int status, String cookie, T data, String error)
	{
		mStatus = status;
		mCookie = cookie;
		mData = data;
		mError = error;
	}
	
	/**
	 * Get HTTP response status, if -1 then failed to send the request
	 * @return status code (e.g 200 for HTTP OK)
	 */
	public int getStatus()
	{
		return mStatus;
	}
	
	/**
	 * Get response cookie
	 * @return
	 */
	public String getCookie()
	{
		return mCookie;
	}
	
	/**
	 * Get error message
	 * @return
	 */
	public String getError()
	{
		return mError;
	}
	
	/**
	 * Get result object
	 * @return
	 */
	public T getData()
	{
		return mData;
	}
}
