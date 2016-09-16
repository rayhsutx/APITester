package com.kaisquare.kainode.tester;

import com.kaisquare.kainode.tester.action.TestActionStatus;

public interface ITester {
		
	VariableCollection doTest(TestStatistics s) throws Exception;
	
	TestActionStatus[] getAllStatus();

}
