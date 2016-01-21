package com.kaisquare.kainode.tester.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public abstract class RequestAction implements IAction {
	
	protected static final String CHECK_NOT_EMPTY = ":notempty";
	protected static final String CHECK_IS_DIGIT = ":isdigit";
	
	private Map<String, String> mVariables = new HashMap<String, String>();
	private static final Pattern PATTERN_VARIABLE = Pattern.compile("(\\{\\{[^\\}]+\\}\\})");
	private static final Pattern PATTERN_NOW_DATE = Pattern.compile("\\$now\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE);
	
	
	public void setVariables(Map<String, String> variables)
	{
		Iterator<Entry<String, String>> iterator = variables.entrySet().iterator();
		while (iterator.hasNext())
		{
			
			Entry<String, String> entry = iterator.next();
			mVariables.put(entry.getKey().toLowerCase(), entry.getValue());
		}
	}
	public void setVariables(Map<String, String> variables, Map<String, String> defaultVariables){
		Iterator<Entry<String, String>> iterator = variables.entrySet().iterator();
		
		while(iterator.hasNext()){
			Entry<String, String> entry = iterator.next();
			String entryValue = entry.getValue();
			
			String[] removeFront = entryValue.split("\\{\\{");
			if(removeFront.length > 1){
				entryValue = removeFront[0];
				
				String[] removeBack = removeFront[1].split("\\}\\}");
				
				entryValue += defaultVariables.get(removeBack[0]);
				
				if(removeBack.length > 1){
					entryValue += removeBack[1];
				}
				
				mVariables.put(entry.getKey().toLowerCase(), entryValue);
				
			}else{
				mVariables.put(entry.getKey().toLowerCase(), entryValue);
			}
			
			if(defaultVariables.get(entry.getKey()) != null){
				mVariables.put(entry.getKey().toLowerCase(), defaultVariables.get(entry.getKey()));
			}
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
		if (Utils.isStringEmpty(str))
			return str;
		
		StringBuilder sb = new StringBuilder();
		Matcher matcher = PATTERN_VARIABLE.matcher(str);
		int index = 0;
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();
			sb.append(str.substring(index, start));
			String name = str.substring(start + 2, end - 2);
			String builtinVar = checkBuiltinVariable(name);
			String var = builtinVar == null ? getVariable(name.toLowerCase()) : builtinVar;
			sb.append(var);
			index = end;
		}
		if (index < str.length())
			sb.append(str.substring(index, str.length()));
		
		return sb.toString();
	}
	
	private String checkBuiltinVariable(String s)
	{
		Matcher m;
		if ((m = PATTERN_NOW_DATE.matcher(s)).find())
		{
			String dateFormat = m.group(1);
			Date now = new Date();
			if ("timestamp".equalsIgnoreCase(dateFormat))
				return Long.toString(now.getTime());

			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			return format.format(now);
		}
		
		return null;
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
		if (rule.startsWith("re:"))
		{
			if (rule.length() < 3)
			{
				AppLogger.e(this, "error checking pattern: invalid rule '%s'", rule);
				return false;
			}
			String pattern = rule.substring(3);
			return Pattern.matches(pattern, value);
		}
		else
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

}
