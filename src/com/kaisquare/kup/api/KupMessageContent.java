package com.kaisquare.kup.api;

import com.kaisquare.utils.Utils;

public class KupMessageContent {
	
	public static final transient int TYPE_TEXT = 0;
	public static final transient int TYPE_IMAGE = 1;
	
	public int type;
	public String mimeType;
	public String name;
	public Object data;
	public long generatedTime;
	
	public KupMessageContent(int type, String mimeType, String name, Object data, long time)
	{
		this.type = type;
		this.mimeType = mimeType;
		this.name = name;
		this.data = data;
		this.generatedTime = time;
	}
	
	public String getString()
	{
		return (String) data;
	}
	
	public String getCachedFilename()
	{
		String cachedName = name;
		if (Utils.isStringEmpty(name))
		{
//			String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
//			cachedName = String.format("%s.%s", 
//					getString(), 
//					TextUtils.isEmpty(extension) ? "file" : extension);
		}
		return String.format("message-%s", cachedName);
	}
	
	public byte[] getBytesData()
	{
		switch (type)
		{
		case TYPE_TEXT:
		case TYPE_IMAGE:
			String content = getString();
			if (content == null)
				return null;
			else
				return content.getBytes();
		}
		
		return null;
	}

	public void release()
	{
//		if (data instanceof Bitmap)
//		{
//			Bitmap bmp = (Bitmap)data;
//			if (!bmp.isRecycled())
//				bmp.recycle();
//			
//			bmp = null;
//		}
	}

	public String getMimeType() {
		return Utils.isStringEmpty(mimeType) ? "application/octet-stream" : mimeType;
	}
}
