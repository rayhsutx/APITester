package com.kaisquare.kainode.tester.action;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.kaisquare.utils.AppLogger;

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
		Map<String, String> map = getVariables();
		Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
		AppLogger.i(this, "Variables:");
		while (iterator.hasNext())
		{
			Entry<String, String> entry = iterator.next();
			AppLogger.i(this, "%s: %s", entry.getKey(), entry.getValue());
		}
		
		return new EmptyActionResult(TestActionStatus.Ok, "", null);
	}
}
