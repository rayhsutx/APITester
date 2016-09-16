package com.kaisquare.kainode.tester.action;

import java.util.Iterator;
import java.util.Map.Entry;

import com.kaisquare.kainode.tester.VariableCollection;
import com.kaisquare.kainode.tester.action.result.ActionResult;
import com.kaisquare.kainode.tester.action.result.EmptyActionResult;
import com.kaisquare.kaisync.utils.AppLogger;

public class PrintTestAction extends RequestAction {

	@Override
	public String getActionName() {
		return "Print";
	}

	@Override
	public String getActionType() {
		return Actions.ACTION_PRINT;
	}

	@Override
	public ActionResult submit(ActionConfiguration config) {
		VariableCollection map = getVariables();
		Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
		AppLogger.i(this, "Variables:");
		while (iterator.hasNext())
		{
			Entry<String, String> entry = iterator.next();
			AppLogger.i(this, "%s: %s", entry.getKey(), entry.getValue());
		}
		
		EmptyActionResult result = new EmptyActionResult(TestActionStatus.Ok, getVariables(), "");		
		return result;
	}
}
