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
		else if (rule.startsWith("cmp:"))
		{
			if (rule.length() < 7)
			{
				AppLogger.e(this, "error checking pattern: invalid rule '%s'", rule);
				return false;
			}
			char[] chars = new String(rule.substring(4)).trim().toCharArray();
			char c1 = chars[0];
			char c2 = chars.length > 2 ? chars[1] : '\0';
			String operator;
			String value1;
			
			if (c2 == '=')
			{
				if (rule.length() < 8)
				{
					AppLogger.e(this, "error checking pattern: invalid rule '%s'", rule);
					return false;
				}
				operator = String.valueOf(chars, 0, 2);
				value1 = String.valueOf(chars, 2, chars.length - 2).trim();
			}
			else
			{
				operator = String.valueOf(c1);
				value1 = String.valueOf(chars, 1, chars.length - 1).trim();
			}
			
			return Pattern.matches("^\\-?\\d+(\\.?\\d+)*$", value) ? 
					compareInDecimal(value, value1, operator) :
					compareInString(value, value1, operator);
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

	private boolean compareInString(String value1, String value2, String operator) {
		int n = value1.compareTo(value2);
		if (">".equalsIgnoreCase(operator))
			return n > 0;
		else if (">=".equalsIgnoreCase(operator))
			return n >= 0;
		else if ("<".equalsIgnoreCase(operator))
			return n < 0;
		else if ("<=".equalsIgnoreCase(operator))
			return n <= 0;
		else if ("=".equalsIgnoreCase(operator))
			return n == 0;
		
		return false;
	}

	private boolean compareInDecimal(String value1, String value2, String operator) {
		Double n1 = Double.valueOf(value1);
		Double n2 = Double.valueOf(value2);
		if (">".equalsIgnoreCase(operator))
			return n1 > n2;
		else if (">=".equalsIgnoreCase(operator))
			return n1 >= n2;
		else if ("<".equalsIgnoreCase(operator))
			return n1 < n2;
		else if ("<=".equalsIgnoreCase(operator))
			return n1 <= n2;
		else if ("=".equalsIgnoreCase(operator))
			return n1 == n2;
		
		return false;
	}

}
