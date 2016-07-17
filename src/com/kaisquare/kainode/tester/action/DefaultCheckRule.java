package com.kaisquare.kainode.tester.action;

import java.util.regex.Pattern;

import com.kaisquare.kainode.tester.ICheckRule;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class DefaultCheckRule implements ICheckRule
{
	protected static final String CHECK_NOT_EMPTY = ":notempty";
	protected static final String CHECK_IS_DIGIT = ":isdigit";
	
	@Override
	public boolean isValid(String rule, String value)
	{
		if (rule.startsWith("re:"))
		{
			if (rule.length() < 3)
			{
				AppLogger.e(this, "error checking pattern: invalid rule '%s'", rule);
				return false;
			}
			String pattern = rule.substring(3);
			return Pattern.matches(pattern, value);
		}
		else
		{
			switch (rule)
			{
			case CHECK_NOT_EMPTY:
				return !Utils.isStringEmpty(value);
			case CHECK_IS_DIGIT:
				return Utils.isDigitString(value);
			default:
				return false;
			}
		}
	}

}
