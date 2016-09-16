package com.kaisquare.gson;

import com.google.gson.Gson;
import com.kaisquare.kainode.tester.jobs.JobConfiguration.JobActionConfiguration;

/**
 * Get the customized Gson object
 */
public class DefaultGsonBuilder {

	public static Gson create()
	{
		com.google.gson.GsonBuilder builder = new com.google.gson.GsonBuilder();
		builder.registerTypeAdapter(JobActionConfiguration.class, new JobActionConfigurationDeserializer());
		
		return builder.create();
	}
}
