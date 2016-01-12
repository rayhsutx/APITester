package com.kaisquare.kainode.tester.action;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class ActionConfiguration {
	//general use
	public String name;
	public int timeout = 10000;
	public int count = 0;
	public Map<String, String> values;
	public LinkedHashMap<String, Object> data;
	public LinkedHashMap<String, String> check;
	//http use only
	public String url;
	public String method = "POST";
	//shell use only
	public String command;
	public String redirect;
	//kaisync use only
	@SerializedName("sync-event")
	public String syncEvent;
	@SerializedName("sync-event-video")
	public String syncEventVideo;
	@SerializedName("sync-command")
	public String syncCommand;
	@SerializedName("bind-command")
	public boolean bindCommand;
	@SerializedName("wait-command")
	public boolean waitCommand;
	@SerializedName("wait-timeout")
	public int commandTimeout;
	@SerializedName("sync-mac-caddress")
	public String syncMacAddress;
	public int threads;
}
