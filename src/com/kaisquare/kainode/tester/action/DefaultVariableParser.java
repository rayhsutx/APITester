package com.kaisquare.kainode.tester.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kaisquare.kainode.tester.IVariableParser;
import com.kaisquare.kaisync.utils.Utils;

class DefaultVariableParser implements IVariableParser
{
	protected static final Pattern PATTERN_VARIABLE = Pattern.compile("(\\{\\{[^\\}]+\\}\\})");
	protected static final Pattern PATTERN_NOW_DATE = Pattern.compile("\\$now\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE);
	
	@Override
	public String parse(Map<String, String> variables, String str)
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
			String var = builtinVar == null ? variables.get(name.toLowerCase()) : builtinVar;
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
}
