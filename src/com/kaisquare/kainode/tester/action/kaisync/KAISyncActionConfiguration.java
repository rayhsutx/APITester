package com.kaisquare.kainode.tester.action.kaisync;

import com.google.gson.annotations.SerializedName;
import com.kaisquare.kainode.tester.action.ActionConfiguration;

public class KAISyncActionConfiguration extends ActionConfiguration {
	
	@SerializedName("sync-event")
	private String syncEvent;
	@SerializedName("sync-event-video")
	private String syncEventVideo;
	@SerializedName("sync-command")
	private String syncCommand;
	@SerializedName("bind-command")
	private boolean bindCommand;
	@SerializedName("wait-command")
	private boolean waitCommand;
	@SerializedName("wait-timeout")
	private int commandTimeout;
	@SerializedName("sync-mac-caddress")
	private String syncMacAddress;
	private int threads;
	
	public String getSyncEvent() {
		return syncEvent;
	}
	
	public String getSyncEventVideo() {
		return syncEventVideo;
	}
	
	public String getSyncCommand() {
		return syncCommand;
	}
	
	public boolean isBindCommand() {
		return bindCommand;
	}
	
	public boolean isWaitCommand() {
		return waitCommand;
	}
	
	public int getCommandTimeout() {
		return commandTimeout;
	}
	
	public String getSyncMacAddress() {
		return syncMacAddress;
	}
	
	public int getThreads() {
		return threads;
	}

}
