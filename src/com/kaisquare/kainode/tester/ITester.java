package com.kaisquare.kainode.tester;

import java.util.Map;

import com.kaisquare.kainode.tester.action.TestActionStatus;

public interface ITester {
		
	Map<String, String> doTest() throws Exception;
	
	TestActionStatus[] getAllStatus();

}
