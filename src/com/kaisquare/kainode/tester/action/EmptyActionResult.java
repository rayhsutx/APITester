package com.kaisquare.kainode.tester.action;

import java.util.Map;

public class EmptyActionResult extends ActionResult {
	
	public EmptyActionResult(TestActionStatus status)
	{
		super(status, null);
	}

	public EmptyActionResult(TestActionStatus status, Object result, Map<String, String> mapping) {
		super(status, result);
	}

	@Override
	protected void assignMappingValues(Map<String, String> createMappingObject,
			TestActionStatus status, Object result, Map<String, String> mapping) {
	}

}
