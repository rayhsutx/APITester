package com.kaisquare.kainode.tester.jobs;

import java.util.List;

import com.kaisquare.kainode.tester.action.ActionConfiguration;

public class JobConfiguration {
	
	public List<JobActionConfiguration> actions;
	public int defaultDelay = 0;
	
	public static class JobActionConfiguration
	{
		public String type;
		public String name;
		public int delay = 0;
		public int retry = 0;
		public int repeat = 0; //-1: infinite, > 0: count of repeat
		public boolean ignoreError = false;
		public ActionConfiguration config;
		public List<String> print;
	}
	
}
