package com.kaisquare.kainode.tester.action;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
		result.parseResult(config.values);
		
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
}
