package com.kaisquare.kainode.tester;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestStatistics {

	public static final String RESULT_PASSED = "Passed";
	public static final String RESULT_FAILED = "Failed";
	public static final String RESULT_UNTESTED = "Untested";
	
	private Map<String, List<String>> files;
	private Map<String, Integer> counts;
	private Map<String, Integer> results;
	private Map<String, TestStatistics> details;
	private String name;
	
	public TestStatistics(String name)
	{
		this.name = name;
		files = new LinkedHashMap<String, List<String>>();
		counts = new LinkedHashMap<String, Integer>();
		results = new LinkedHashMap<String, Integer>();
		details = new LinkedHashMap<String, TestStatistics>();
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addStatistics(TestStatistics s)
	{
		String[] types = {
			TestStatistics.RESULT_PASSED,
			TestStatistics.RESULT_FAILED,
			TestStatistics.RESULT_UNTESTED
		};
		for (String type : types)
		{
			Integer count = counts.get(type);
			if (count == null)
				count = Integer.valueOf(0);
			counts.put(type, count.intValue() + s.getNumberOfFiles(type));
		}
		increaseResultNumbers(TestStatistics.RESULT_PASSED, s.getNumberOfActions(TestStatistics.RESULT_PASSED));
		increaseResultNumbers(TestStatistics.RESULT_FAILED, s.getNumberOfActions(TestStatistics.RESULT_FAILED));
		details.put(s.getName(), s);
	}
	
	public synchronized void addResultFiles(String type, List<String> files)
	{
		Integer count = counts.get(type);
		if (count == null)
			count = Integer.valueOf(0);
		List<String> list = this.files.get(type);
		if (list == null)
		{
			list = new LinkedList<String>();
			this.files.put(type, list);
		}
		list.addAll(files);
		counts.put(type, count.intValue() + files.size());
	}
	
	public List<String> getResultFiles(String type)
	{
		return files.get(type);
	}
	
	public int getNumberOfFiles(String type)
	{
		return counts.get(type);
	}
	
	public synchronized void increaseResultNumbers(String type, int value)
	{
		Integer n = results.get(type);
		if (n == null)
			n = Integer.valueOf(0);
		results.put(type, n.intValue() + value);
	}
	
	public Integer getNumberOfActions(String type)
	{
		return results.get(type);
	}

}
