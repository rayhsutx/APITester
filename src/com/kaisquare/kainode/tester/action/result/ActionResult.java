package com.kaisquare.kainode.tester.action.result;

import java.util.Map;
import java.util.Map.Entry;

import com.kaisquare.kainode.tester.VariableCollection;
import com.kaisquare.kainode.tester.action.TestActionStatus;

public abstract class ActionResult {
	
	private TestActionStatus mStatus;
	private Object mResult;
	private String mReason;
	protected VariableCollection mMapVariables;
	
	public ActionResult(TestActionStatus status, VariableCollection variables, Object result)
	{
		mStatus = status;
		mResult = result;
		mMapVariables = variables;
	}
	
	public void setReason(String reason)
	{
		mReason = reason;
	}
	
	public String getReason()
	{
		return mReason;
	}
	
	protected Map<String, String> parseVariables(Map<String, String> values)
	{
		if (values != null)
		{
			@SuppressWarnings("unchecked")
			Entry<String, String>[] entries = values.entrySet().toArray(new Entry[0]);
			for (Entry<String, String> e : entries)
			{
				parseVariable(e.getKey(), getVariables().parseVariable(e.getValue()), e.getValue());
			}
		}
		
		return values;
	}

	/**
	 * Parse the result and put the values which are from result into variables for next action 
	 * @param mapping specify variable in the result 
	 */
	public void parseResult(Map<String, String> mapping)
	{
		if (mStatus == TestActionStatus.Ok)
			assignMappingValues(mStatus, mResult, parseVariables(mapping));
	}
	
	/**
	 * Put a variable
	 * @param name name of the variable
	 * @param value the actual value 
	 */
	public void putVariable(String name, String value)
	{
		mMapVariables.put(name, value);
	}
	
	/**
	 * Put all variable
	 * @param variables
	 */
	public void putVariableAll(Map<String, String> variables)
	{
		mMapVariables.putAll(variables);
	}

	public Object getResult()
	{
		return mResult;
	}
	
	public void setStatus(TestActionStatus status)
	{
		mStatus = status;
	}
	
	public TestActionStatus getStatus()
	{
		return mStatus;
	}
	
	public String getValue(String key)
	{
		return mMapVariables.get(key);
	}
	
	public VariableCollection getVariables()
	{
		return mMapVariables;
	}
	
	protected void parseVariable(String name, String value, String rawValue)
	{
		putVariable(name, value);
	}
	
	protected abstract void assignMappingValues(TestActionStatus status, Object result, Map<String, String> mapping);
}
