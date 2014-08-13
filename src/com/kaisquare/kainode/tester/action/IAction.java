package com.kaisquare.kainode.tester.action;

public interface IAction {
	
	String getActionName();
	
	String getActionType();
	
	ActionResult submit(ActionConfiguration config);

}
