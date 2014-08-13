package com.kaisquare.kainode.tester.action;

import java.util.HashMap;
import java.util.Map;

public abstract class ActionResult {
	
	private TestActionStatus mStatus;
	private Object mResult;
	protected Map<String, String> mMappingValues;
	
	public ActionResult(TestActionStatus status, Object result)
	{
		mStatus = status;
		mResult = result;
		mMappingValues = createMappingObject();
	}
	
	public void parseResult(Map<String, String> mapping)
	{
		if (mStatus == TestActionStatus.Ok)
			assignMappingValues(createMappingObject(), mStatus, mResult, mapping);
	}
	
	protected Map<String, String> createMappingObject()
	{
		return new HashMap<String, String>();
	}
	
	protected void putVariable(String name, String value)
	{
		mMappingValues.put(name, value);
	}
	
	protected void putVariableAll(Map<String, String> variables)
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
