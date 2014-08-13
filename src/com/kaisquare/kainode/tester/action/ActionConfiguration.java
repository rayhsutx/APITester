package com.kaisquare.kainode.tester.action;

import java.util.LinkedHashMap;
import java.util.Map;

public class ActionConfiguration {
	//general use
	public String name;
	public int timeout = 10000;
	public Map<String, String> values;
	//http use only
	public String url;
	public String method = "POST";
	public LinkedHashMap<String, Object> data;
	public LinkedHashMap<String, String> check;
	//shell use only
	public String command;
	public String redirect;
}
