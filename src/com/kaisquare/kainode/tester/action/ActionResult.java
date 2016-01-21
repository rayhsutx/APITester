package com.kaisquare.kainode.tester.action;

import java.util.HashMap;
import java.util.Map;

public abstract class ActionResult {
	
	private TestActionStatus mStatus;
	private Object mResult;
	private String mReason;
	protected Map<String, String> mMappingValues;
	
	public ActionResult(TestActionStatus status, Object result)
	{
		mStatus = status;
		mResult = result;
		mMappingValues = createMappingObject();
	}
	
	protected void setReason(String reason)
	{
		mReason = reason;
	}
	
	public String getReason()
	{
		return mReason;
	}
	
	/**
	 * Parse the result and put the values which are from result into variables for next action 
	 * @param mapping specify variable in the result 
	 */
	public void parseResult(Map<String, String> mapping)
	{
		if (mStatus == TestActionStatus.Ok)
			assignMappingValues(createMappingObject(), mStatus, mResult, mapping);
	}
	
	protected Map<String, String> createMappingObject()
	{
		return new HashMap<String, String>();
	}
	
	/**
	 * Put a variable
	 * @param name name of the variable
	 * @param value the actual value 
	 */
	public void putVariable(String name, String value)
	{
		mMappingValues.put(name, value);
	}
	
	/**
	 * Put all variable
	 * @param variables
	 */
	public void putVariableAll(Map<String, String> variables)
	{
		mMappingValues.putAll(variables);
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
		return mMappingValues.get(key);
	}
	
	public Map<String, String> getVariables()
	{
		Map<String, String> map = createMappingObject();
		map.putAll(mMappingValues);
		
		return map;
	}
	
	protected abstract void assignMappingValues(Map<String, String> createMappingObject,
			TestActionStatus status, Object result, Map<String, String> mapping);
}
