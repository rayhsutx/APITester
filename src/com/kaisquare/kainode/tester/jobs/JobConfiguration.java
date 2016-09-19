package com.kaisquare.kainode.tester.jobs;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.kaisquare.kainode.tester.action.ActionConfiguration;

public class JobConfiguration {
	
	private List<JobActionConfiguration> actions;
	@SerializedName("failure-on-actions")
	private List<JobActionConfiguration> failedActions;
	private int defaultDelay = 0;
	private int loop = 1;
	private int duration = 0;

	public int getLoop() {
		return loop;
	}

	public int getDuration() {
		return duration;
	}

	public List<JobActionConfiguration> getActions() {
		return actions;
	}
	
	public List<JobActionConfiguration> getFailedActions()
	{
		return failedActions;
	}

	protected int getDefaultDelay() {
		return defaultDelay;
	}

	public static class JobActionConfiguration
	{
		private String type;
		private String name;
		private int delay = 0;
		private int retry = 0;
		private int repeat = 0; //-1: infinite, > 0: count of repeat
		private boolean ignoreError = false;
		@SerializedName("max-response-time")
		private int maxResponseTime = 0;
		private ActionConfiguration config;
		private List<String> print;
		
		public String getType() {
			return type;
		}
		
		public String getName() {
			return name;
		}
		
		public int getDelay() {
			return delay;
		}

		public void setDelay(int defaultDelay) {
			if (defaultDelay >= 0)
				delay = defaultDelay;
		}
		
		public int getRetry() {
			return retry;
		}
		
		public int getRepeat() {
			return repeat;
		}
		
		public boolean isIgnoreError() {
			return ignoreError;
		}

		public int getMaxResponseTime() {
			return maxResponseTime;
		}
		
		public ActionConfiguration getConfig() {
			return config;
		}
		
		public List<String> getPrint() {
			return print;
		}
	}
	
}
