package com.kaisquare.kainode.tester.action;

import com.kaisquare.kainode.tester.ICheckRule;
import com.kaisquare.kainode.tester.IVariableParser;

public final class Defaults
{
	public static final IVariableParser defaultVariableParser = new DefaultVariableParser();
	public static final ICheckRule defaultCheckRule = new DefaultCheckRule();
}
