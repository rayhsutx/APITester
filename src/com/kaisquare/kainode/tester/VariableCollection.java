package com.kaisquare.kainode.tester;

import java.util.concurrent.ConcurrentHashMap;

import com.kaisquare.kainode.tester.action.Defaults;

public class VariableCollection extends ConcurrentHashMap<String, String>
{
	@Override
	public String put(String key, String value)
	{
		if (key == null)
			return null;
		else if (value == null)
			return super.put(key, "");
		else
			return super.put(key, value);
	}

	private static final long serialVersionUID = 1L;
	
	public String putAndParseVariable(String name, String str)
	{
		String parsed = parseVariable(str);
		put(name, parsed);
		
		return parsed;
	}
	
	public String parseVariable(String str)
	{
		return Defaults.defaultVariableParser.parse(this, str);
	}
}
