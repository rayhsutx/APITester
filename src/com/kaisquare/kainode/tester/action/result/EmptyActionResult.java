package com.kaisquare.kainode.tester.action.result;

import java.util.Map;

import com.kaisquare.kainode.tester.VariableCollection;
import com.kaisquare.kainode.tester.action.TestActionStatus;

public class EmptyActionResult extends ActionResult {
	
	public EmptyActionResult(TestActionStatus status, VariableCollection variables)
	{
		super(status, variables, null);
	}

	public EmptyActionResult(TestActionStatus status, VariableCollection variables, Object result) {
		super(status, variables, result);
	}

	@Override
	protected void assignMappingValues(TestActionStatus status, Object result, Map<String, String> mapping) {
	}

}
