package com.kaisquare.kainode.tester.action;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class ActionConfiguration {

	//general use
	private String name;
	private int timeout = 10000;
	private int count = 0;
	private Map<String, String> values;
	private LinkedHashMap<String, Object> data;
	private LinkedHashMap<String, String> check;
	
	public String getName() {
		return name;
	}
	
	protected void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public int getCount() {
		return count;
	}
	
	public Map<String, String> getValues() {
		return values;
	}
	
	public LinkedHashMap<String, Object> getData() {
		return data;
	}
	
	public LinkedHashMap<String, String> getCheck() {
		return check;
	}
}
