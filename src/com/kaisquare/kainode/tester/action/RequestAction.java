package com.kaisquare.kainode.tester.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kaisquare.utils.AppLogger;
import com.kaisquare.utils.Utils;

public abstract class RequestAction implements IAction {
	
	protected static final String CHECK_NOT_EMPTY = ":notempty";
	protected static final String CHECK_IS_DIGIT = ":isdigit";
	
	private Map<String, String> mVariables = new HashMap<String, String>();
	private Pattern pattern = Pattern.compile("(\\{\\{[^\\}]+\\}\\})");
	
	public void setVariables(Map<String, String> variables)
	{
		Iterator<Entry<String, String>> iterator = variables.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, String> entry = iterator.next();
			mVariables.put(entry.getKey().toLowerCase(), entry.getValue());
		}
	}
	
	public String getVariable(String key)
	{
		return mVariables.get(key);
	}
	
	public Map<String, String> getVariables()
	{
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(mVariables);
		return map;
	}
	
	public String parseVariables(String str)
	{
		StringBuilder sb = new StringBuilder();
		Matcher matcher = pattern.matcher(str);
		int index = 0;
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();
			sb.append(str.substring(index, start));
			String name = str.substring(start + 2, end - 2);
			String var = getVariable(name.toLowerCase());
			sb.append(var);
			index = end;
		}
		if (index < str.length())
			sb.append(str.substring(index, str.length()));
		
		return sb.toString();
	}
	
	protected void checkResult(ActionResult result, Map<String, String> check)
	{
		if (check != null)
		{
			if (!checkResult(result.getVariables(), check))
				result.setStatus(TestActionStatus.Failed);
		}
	}
	
	public boolean checkResult(Map<String, String> variables, Map<String, String> check)
	{
		boolean ret = true;
		
		Iterator<Entry<String, String>> iterator = check.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, String> entry = iterator.next();
			String value = variables.get(entry.getKey().toLowerCase());
			if (!hasCheckRule(entry.getValue(), value))
			{
				if (value == null || !value.equals(entry.getValue()))
				{
					AppLogger.i(this, "FAILED: %s=%s, not '%s'", entry.getKey(), value, entry.getValue());
					ret = false;
				}
			}
		}
		
		return ret;
	}
	
	protected boolean hasCheckRule(String rule, String value)
	{
		switch (rule)
		{
		case CHECK_NOT_EMPTY:
			return !Utils.isStringEmpty(value);
		case CHECK_IS_DIGIT:
			return Utils.isDigitString(value);
		default:
			return false;
		}
	}

}
