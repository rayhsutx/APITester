package com.kaisquare.kainode.tester.action.http;

import com.kaisquare.kainode.tester.action.ActionConfiguration;

public class HttpActionConfiguration extends ActionConfiguration {
	
	private String url;
	private String method = "POST";
	
	public String getUrl() {
		return url;
	}
	
	public String getMethod() {
		return method;
	}

}
