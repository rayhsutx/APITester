package com.kaisquare.kainode.tester;

import java.util.HashMap;
import java.util.Map;

import com.kaisquare.kaisync.utils.Utils;

public class CommandArgs
{
	private HashMap<String, String> mArgs;
	
	public CommandArgs(String[] args)
	{
		mArgs = new HashMap<String, String>();
		parseCommandLine(args);
	}
	
	private void parseCommandLine(String[] args)
	{			
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			
			if (Utils.isStringEmpty(arg))
				continue;
			else if (arg.startsWith("--") && (i + 1) < args.length && !args[i + 1].startsWith("--"))
				mArgs.put(arg, args[++i]);
			else
				mArgs.put(arg, "");
		}
	}
	
	public String getValue(String key)
	{
		if (mArgs.containsKey(key))
			return mArgs.get(key);
		else
			return null;
	}
	
	public Map<String, String> getArguments()
	{
		return mArgs;
	}
}
