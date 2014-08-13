package com.kaisquare.kainode.tester.jobs;

import java.util.List;

import com.kaisquare.kainode.tester.action.ActionConfiguration;

public class JobConfiguration {
	
	public List<JobActionConfiguration> actions;
	
	public static class JobActionConfiguration
	{
		public String type;
		public String name;
		public int delay = 1000;
		public int retry = 0;
		public boolean ignoreError = false;
		public ActionConfiguration config;
	}
	
}
