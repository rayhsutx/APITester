package com.kaisquare.kainode.tester.action.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.kaisquare.kainode.tester.action.ActionConfiguration;
import com.kaisquare.kainode.tester.action.Actions;
import com.kaisquare.kainode.tester.action.RequestAction;
import com.kaisquare.kainode.tester.action.TestActionStatus;
import com.kaisquare.kainode.tester.action.result.ActionResult;
import com.kaisquare.kainode.tester.action.result.JsonActionResult;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class HttpRequestAction extends RequestAction {
	
	public static final List<String> COOKIES = Collections.synchronizedList(new ArrayList<String>());
	
	private HttpActionConfiguration config;
	
	public String getActionName()
	{
		return config.getName(); 
	}
	
	@Override
	public Class<? extends ActionConfiguration> getConfigurationClass()
	{
		return HttpActionConfiguration.class;
	}

	@Override
	public ActionResult submit(ActionConfiguration c) {
		
		config = (HttpActionConfiguration) c;
		HttpURLConnection conn = null;
		InputStream in = null;
		OutputStream os = null;
		ActionResult result = null;
		String url = parseVariable(config.getUrl());
		
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			AppLogger.i(this, "url : %s", url);
			byte[] postBytes = getPostData(config); 
			conn.setUseCaches(false);
			conn.setRequestMethod(config.getMethod());
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;");
			conn.setRequestProperty("Connection", "close");
			if (postBytes.length > 0)
			{
				conn.setRequestProperty("Content-Length", Integer.toString(postBytes.length));
				conn.setDoOutput(true);
			}
			AppLogger.d(this, "set connect timeout: %s", config.getTimeout());
			conn.setConnectTimeout(config.getTimeout());
			
			StringBuilder sbCookie = new StringBuilder();
			if (COOKIES.size() > 0)
			{
				for (String cookie : COOKIES)
				{
					if (sbCookie.length() > 0)
						sbCookie.append(";");
					sbCookie.append(cookie);
				}
				conn.setRequestProperty("Cookie", sbCookie.toString());
			}
			
			conn.connect();
			AppLogger.i(this, "connected %s", url);			
			
			if(postBytes.length > 0){
				os = conn.getOutputStream();
				os.write(postBytes);
				os.flush();
				os.close();
			}
			
			
			
			in = new BufferedInputStream(conn.getInputStream());			
			byte[] b = new byte[8192];
			int read = 0;
			StringBuilder sbResult = new StringBuilder();
			while ((read = in.read(b)) > 0)
			{
				String s = new String(b, 0, read);
				sbResult.append(s);
			}
			
			int status = conn.getResponseCode();
			AppLogger.d(this, "http status: %d", status);
			
			String cookie = conn.getHeaderField("Set-Cookie");
			if (!Utils.isStringEmpty(cookie))
			{
				COOKIES.clear();
				cookie = cookie.split("\\;", 2)[0];
				if (!COOKIES.contains(cookie))
					COOKIES.add(cookie);
			}
			
			AppLogger.d(this, "response: %s", sbResult.toString());
			result = new JsonActionResult(TestActionStatus.Ok, getVariables(), sbResult.toString());
			result.putVariable("__COOKIE__", cookie);
			result.parseResult(config.getValues());
			
			checkResult(result, config.getCheck());			
		} catch (Exception e) {
			AppLogger.e(this, e, "HttpRequestAction: " + url);
			result = new JsonActionResult(TestActionStatus.Error, getVariables(), "");
			result.setReason(e.getMessage());
		} finally {
			if (conn != null)
			{
				conn.disconnect();
				conn = null;
			}
			if (in != null)
			{
				try {
					in.close();
				} catch (IOException e) {}
			}
			if (os != null)
			{
				try {
					os.close();
				} catch (IOException e) {}
			}
		}
		
		return result;
	}

	private byte[] getPostData(ActionConfiguration config) {
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<String, Object>> iterator = config.getData().entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, Object> entry = iterator.next();
			if (Utils.isStringEmpty(entry.getKey()))
				continue;
			
			String value;
			try {
				value = URLEncoder.encode(parseVariable(String.valueOf(entry.getValue())), "utf8");
			} catch (UnsupportedEncodingException e) {
				AppLogger.e(this, e, "");
				continue;
			}
			
			if (sb.length() > 0)
				sb.append("&");
			
			sb.append(String.format("%s=%s", 
					entry.getKey(),
					value));
		}
		String postData = sb.toString();
		AppLogger.v(this, "post: %s", postData);
		
		return postData.getBytes();
	}

	@Override
	public String getActionType() {
		return Actions.ACTION_HTTP;
	}
}
