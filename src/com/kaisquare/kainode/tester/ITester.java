package com.kaisquare.kainode.tester;

import com.kaisquare.kainode.tester.action.TestActionStatus;

public interface ITester {
		
	VariableCollection doTest() throws Exception;
	
	TestActionStatus[] getAllStatus();

}
