package com.kaisquare.kainode.tester.action;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.kaisquare.kainode.tester.VariableCollection;
import com.kaisquare.kainode.tester.action.result.ActionResult;
import com.kaisquare.kaisync.utils.AppLogger;

public abstract class RequestAction implements IAction {
	
	private VariableCollection mVariables;
	
	public void setVariables(VariableCollection variables)
	{
		mVariables = variables;
	}
	
	public String getVariable(String key)
	{
		return mVariables.get(key);
	}
	
	public VariableCollection getVariables()
	{
		return mVariables;
	}
	
	protected void checkResult(ActionResult result, Map<String, String> check)
	{
		if (check != null)
		{
			if (!checkResult(result.getVariables(), check))
				result.setStatus(TestActionStatus.Failed);
		}
	}
	
	public boolean checkResult(VariableCollection variables, Map<String, String> check)
	{
		boolean ret = true;
		
		Iterator<Entry<String, String>> iterator = check.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, String> entry = iterator.next();
			String value = variables.get(entry.getKey().toLowerCase());
			if (!Defaults.defaultCheckRule.isValid(entry.getValue(), value))
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
	
	protected String parseVariable(String str)
	{
		return mVariables.parseVariable(str);
	}
	
	@Override
	public Class<? extends ActionConfiguration> getConfigurationClass()
	{
		return ActionConfiguration.class;
	}

}
