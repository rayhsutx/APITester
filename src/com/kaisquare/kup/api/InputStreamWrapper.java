package com.kaisquare.kup.api;

import java.io.InputStream;

/**
 * This wraps some other information of {@link InputStream}
 */
public class InputStreamWrapper {
	
	private String mName;
	private String mFullName;
	private String mMimeType;
	private long mLength;
	private InputStream mInputStream;
	
	public InputStreamWrapper(String name, String fullname, String mimeType, long length, InputStream in)
	{
		mName = name;
		mFullName = fullname;
		mMimeType = mimeType;
		mLength = length;
		mInputStream = in;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String getFullName()
	{
		return mFullName;
	}
	
	public String getMimeType()
	{
		return mMimeType;
	}

	public long getLength()
	{
		return mLength;
	}
	
	public InputStream getInputStream()
	{
		return mInputStream;
	}
}
