package com.kaisquare.kainode.tester.action;

import com.kaisquare.kainode.tester.action.result.ActionResult;

public interface IAction {
	
	String getActionName();
	
	String getActionType();
	
	/**
	 * Get proper {@link ActionConfiguration} class for each own derived {@link RequestAction}
	 * @return the proper {@link ActionConfiguration}
	 */
	Class<? extends ActionConfiguration> getConfigurationClass();
	
	ActionResult submit(ActionConfiguration c);

}
