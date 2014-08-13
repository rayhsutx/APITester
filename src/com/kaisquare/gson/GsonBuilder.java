package com.kaisquare.gson;

import com.google.gson.Gson;

/**
 * Get the customized Gson object
 */
public class GsonBuilder {		
	public static Gson create()
	{
		com.google.gson.GsonBuilder builder = new com.google.gson.GsonBuilder();
//		builder.registerTypeAdapter(Bitmap.class, new BitmapJsonSerializable());
		
		return builder.create();
	}
}
