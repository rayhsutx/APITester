package com.kaisquare.kainode.tester;

import java.util.Map;

import org.w3c.dom.Element;

import com.kaisquare.kainode.tester.action.TestActionStatus;

public interface ITester {
		
	Map<String, String> doTest() throws Exception;
	
	TestActionStatus[] getAllStatus();

	Map<String, String> doTest(Element fileElement) throws Exception;

}
