package com.kaisquare.kainode.tester;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestStatistics {

	public static final String RESULT_PASSED = "Passed";
	public static final String RESULT_FAILED = "Failed";
	public static final String RESULT_UNTESTED = "Untested";
	
	private Map<String, List<Job>> jobs;
	private Map<String, Integer> jobResults;
	private List<TestStatistics> details;
	private Map<String, Integer> detailResults;
	private String name;
	
	private static final String[] Alltypes = {
		TestStatistics.RESULT_PASSED,
		TestStatistics.RESULT_FAILED,
		TestStatistics.RESULT_UNTESTED
	};
	
	public TestStatistics(String name)
	{
		this.name = name;
		jobs = new LinkedHashMap<String, List<Job>>();
		jobResults = new LinkedHashMap<String, Integer>();
		details = new LinkedList<TestStatistics>();
		detailResults = new LinkedHashMap<String, Integer>();
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addDetail(TestStatistics s)
	{
		if (s == null)
			return;
		
		for (String type : Alltypes)
		{
			detailResults.put(type, getIntValue(detailResults.get(type)) + s.getNumberOfJobResult(type));
		}
		details.add(s);
	}
	
	public synchronized void addResultJob(String type, Job job)
	{
		List<Job> list = this.jobs.get(type);
		if (list == null)
		{
			list = new LinkedList<Job>();
			this.jobs.put(type, list);
		}
		list.add(job);
		jobResults.put(type, getIntValue(jobResults.get(type)) + 1);
	}
	
	public synchronized void addResultJobs(String type, List<Job> jobs)
	{
		for (Job job : jobs)
			addResultJob(type, job);
	}
	
	public List<Job> getResultJobs(String type)
	{
		return jobs.get(type);
	}
	
	public int getNumberOfJobs(String type)
	{
		return jobs.get(type) == null ? 0 : jobs.get(type).size();
	}
	
	public int getNumberOfDetails()
	{
		return details.size();
	}
	
	public synchronized void increaseResultNumbers(String type, int value)
	{
		jobResults.put(type, getIntValue(jobResults.get(type)) + value);
	}
	
	public Integer getNumberOfJobResult(String type)
	{
		return getIntValue(jobResults.get(type));
	}
	
	public Integer getNumberOfDetailResult(String type)
	{
		return getIntValue(detailResults.get(type));
	}
	
	private int getIntValue(Integer n)
	{
		return n == null ? 0 : n.intValue();
	}
	
	@Override
	public String toString()
	{
		return String.format("Pass: jobs=%d, actions=%d, Failed: jobs=%d, actions=%d",
					getNumberOfJobs(TestStatistics.RESULT_PASSED),
					getNumberOfJobResult(TestStatistics.RESULT_PASSED),
					getNumberOfJobs(TestStatistics.RESULT_FAILED),
					getNumberOfJobResult(TestStatistics.RESULT_FAILED));
	}
	
	public static class Job
	{
		public long index;
		public long group;
		public String name;
		public long start;
		public long end;
		private double spent;
		
		public Job()
		{
		}
		
		public Job(long index, long group, String name, long start, long end)
		{
			this.index = index;
			this.group = group;
			this.name = name;
			this.start = start;
			this.end = end;
			calculateSpent();
		}
		
		public void calculateSpent()
		{
			spent = (end - start) / 1000000f;
			spent = Math.round(spent * 100) / (double)100;
		}
		
		public double getSpent()
		{
			return spent;
		}
	}

}
