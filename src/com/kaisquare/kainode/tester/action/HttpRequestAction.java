package com.kaisquare.kainode.tester.action;

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

import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class HttpRequestAction extends RequestAction {
	
	public static final List<String> COOKIES = Collections.synchronizedList(new ArrayList<String>());
	
	private ActionConfiguration mConfig;
	
	public String getActionName()
	{
		return mConfig.name; 
	}

	@Override
	public ActionResult submit(ActionConfiguration config) {
		
		mConfig = config;
		HttpURLConnection conn = null;
		InputStream in = null;
		OutputStream os = null;
		ActionResult result = null;
		String url = parseVariables(config.url);
		
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			byte[] postBytes = getPostData(config); 
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setRequestMethod(config.method);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;");
			conn.setRequestProperty("Content-Length", Integer.toString(postBytes.length));
			AppLogger.d(this, "set connect timeout: %s", config.timeout);
			conn.setConnectTimeout(config.timeout);
			
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

			AppLogger.i(this, "connect %s", url);
			os = conn.getOutputStream();
			os.write(postBytes);
			
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
				cookie = cookie.split("\\;", 2)[0];
				if (!COOKIES.contains(cookie))
					COOKIES.add(cookie);
			}
			
			AppLogger.d(this, "response: %s", sbResult.toString());
			result = new JsonActionResult(TestActionStatus.Ok, sbResult.toString());
			result.putVariableAll(getVariables());
			result.putVariable("__COOKIE__", cookie);
			result.parseResult(config.values);
			
			checkResult(result, config.check);			
		} catch (Exception e) {
			AppLogger.e(this, e, "HttpRequestAction: " + url);
			result = new JsonActionResult(TestActionStatus.Error, "");
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
		Iterator<Entry<String, Object>> iterator = config.data.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, Object> entry = iterator.next();
			if (Utils.isStringEmpty(entry.getKey()))
				continue;
			
			String value;
			try {
				value = URLEncoder.encode(parseVariables(String.valueOf(entry.getValue())), "utf8");
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
