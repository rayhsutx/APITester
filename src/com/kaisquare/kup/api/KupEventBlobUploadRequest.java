package com.kaisquare.kup.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.gson.annotations.SerializedName;

public class KupEventBlobUploadRequest extends KupRequest {
	
	@SerializedName("eventId")
	private String eventId;
	
	private transient InputStreamWrapper mInputStream;

	public KupEventBlobUploadRequest(String eventId, String mimeType, File f) throws FileNotFoundException {
		this(eventId, new InputStreamWrapper("attachment", f.getName(), mimeType, f.length(), new BufferedInputStream(new FileInputStream(f))));
	}
	
	public KupEventBlobUploadRequest(String eventId, String mimeType, File f, IKupResponseListener listener) throws FileNotFoundException {
		this(eventId, 
				new InputStreamWrapper("attachment", f.getName(), mimeType, f.length(), 
						new BufferedInputStream(new FileInputStream(f))), 
			listener);
	}
	
	public KupEventBlobUploadRequest(String eventId, InputStreamWrapper in)
	{
		this(eventId, in, null);
	}

	public KupEventBlobUploadRequest(String eventId, InputStreamWrapper in, IKupResponseListener listener) {
		super(listener);
		
		this.eventId = eventId; 
		mInputStream = in;
	}
	
	@Override
	protected InputStreamWrapper getInputStream() {
		return mInputStream;
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.EVENT_BLOB_UPLOAD;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupEventBlobUploadResult.class;
	}
	
	@Override
	protected void onResult(String api, KupResponseResult result) {
		mInputStream = null;
		super.onResult(api, result);
	}

	@Override
	protected void setPostData() {
	}

	public static class KupEventBlobUploadResult extends KupResponseResult
	{
		@SerializedName("blob-id")
		public String blobId;
	}
}
