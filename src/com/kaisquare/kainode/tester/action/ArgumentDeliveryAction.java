package com.kaisquare.kainode.tester.action;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.kaisquare.kaisync.utils.AppLogger;

public class ArgumentDeliveryAction extends RequestAction {

	@Override
	public String getActionName() {
		return "ArgumentDelivery";
	}

	@Override
	public String getActionType() {
		return Actions.ACTION_ARGUMENT_DELIVERY;
	}

	@Override
	public ActionResult submit(ActionConfiguration config) {
		ArgumentDeliveryActionResult result = new ArgumentDeliveryActionResult(TestActionStatus.Ok, config.data);
		result.putVariableAll(getVariables());
		result.parseResult(parseVariables(config.values));
		AppLogger.i("", "Arguments parsed", "");
		return result;
	}

	public static class ArgumentDeliveryActionResult extends ActionResult
	{

		public ArgumentDeliveryActionResult(TestActionStatus status,
				Object result) {
			super(status, result);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void assignMappingValues(
				Map<String, String> createMappingObject,
				TestActionStatus status, Object result,
				Map<String, String> mapping) {
			
			Map<String, String> map = (Map<String, String>) result;
			Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext())
			{
				Entry<String, String> entry = iterator.next();
				putVariable(entry.getKey(), entry.getValue());
			}
			
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, String> parseVariables(Map<String, String> values) {
		if (values != null)
		{
			Entry<String, String>[] entries = values.entrySet().toArray(new Entry[0]);
			for (Entry<String, String> e : entries)
			{
				values.put(e.getKey(), parseVariables(e.getValue()));
			}
		}
		
		return values;
	}
}
