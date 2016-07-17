package com.kaisquare.kainode.tester.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.kaisquare.kainode.tester.APITester;
import com.kaisquare.kainode.tester.ITester;
import com.kaisquare.kainode.tester.VariableCollection;
import com.kaisquare.kainode.tester.action.ActionResult;
import com.kaisquare.kainode.tester.action.Actions;
import com.kaisquare.kainode.tester.action.Actions.ActionNotFoundException;
import com.kaisquare.kainode.tester.action.RequestAction;
import com.kaisquare.kainode.tester.action.TestActionStatus;
import com.kaisquare.kainode.tester.jobs.JobConfiguration.JobActionConfiguration;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class TestJob implements ITester {
	
	private JobConfiguration mConfig;
	private TestActionStatus[] mAllStatus;
	private VariableCollection mVariables;
	private int success = 0, failed = 0, error = 0;

	public TestJob(String jobFile, VariableCollection variables) throws IOException
	{
		Path path = Paths.get(jobFile);
		BufferedReader reader = null;
		
		try {
			reader = Files.newBufferedReader(path, Charset.forName("utf8"));
			mConfig = new Gson().fromJson(reader, JobConfiguration.class);
		} catch (IOException e) {
			throw new IOException(e);
		} finally {
			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e){}
			}
		}
		
		mVariables = variables;
	}

	@Override
	public VariableCollection doTest() throws Exception {
		
		ActionResult result = null;
		double totalSpent = 0;
		Exception ex = null;
		try {
			if (mConfig.actions.size() > 0)
			{
				mAllStatus = new TestActionStatus[mConfig.actions.size()];
				int n = 0, retry = 0;
				for (JobActionConfiguration act : mConfig.actions)
				{					
					if (APITester.isQuitted())
						break;
					
					RequestAction action = (RequestAction)Actions.create(act.type);
					action.setVariables(mVariables);
					
					int repeat = 0;
					long start, end;
					double spent;
					for (;;) {
						if (act.delay > 0)
						{
							AppLogger.i(this, "delay starting action '%s' in %d ms", act.name, act.delay);
							Thread.sleep(act.delay);
						}
						else
							act.delay = mConfig.defaultDelay;
						
						try {
							ex = null;
							AppLogger.i(this, "\n>>>>>>>>>> Starting Action '%s'... <<<<<<<<<< (repeat %d)", act.name, repeat);
							start = System.nanoTime();
							result = action.submit(act.config);
							end = System.nanoTime();
							mAllStatus[n] = result.getStatus();
							spent = (end - start) / 1000000f;
							totalSpent += spent;
							AppLogger.i(this, ">>>>>>>>>> Action '%s'...%s <<<<<<<<<< (spent: %f ms)\n", act.name, result.getStatus(), spent);
							
							if (!act.ignoreError && result.getStatus() != TestActionStatus.Ok)
							{
								if (act.retry > retry)
								{
									retry++;
									AppLogger.i(this, "retry action '%s' %d/%d", act.name, retry, act.retry);
									continue;
								}
								printVariables(action.getVariables());
								throw new ActionFailedException(
										result.getStatus() == TestActionStatus.Error ?
												result.getReason() :
												"action '" + act.name + "' failed");
							}
						} catch (Exception e) {
							if (!act.ignoreError)
							{
								ex = e;
								break;
							}
						}
						
						if (act.repeat < 0)
							continue;
						else if (act.repeat > repeat)
						{
							repeat++;
							continue;
						}
						break;
					}
					
					if (ex == null)
					{
						switch (result.getStatus())
						{
						case Ok:
							success++;
							break;
						case Failed:
							failed++;
							break;
						case Error:
							error++;
							break;
						default:
						}
					}
					
					if (act.print != null)
					{
						AppLogger.i(this, "print variables >>>>>");
						for (String var : act.print)
						{
							if (!Utils.isStringEmpty(var))
								AppLogger.i(this, "%s=%s", var, mVariables.get(var));
						}
						AppLogger.i(this, "<<<<<");
					}
					
					if (!act.ignoreError && ex != null)
						throw ex;
				}
			}
			else
				AppLogger.i(this, "No actions");
		} catch (ActionNotFoundException e) {
			AppLogger.e(this, "Test job stopped: %s", e.getMessage());
			error++;
		} catch (ActionFailedException e) {
			AppLogger.e(this, "Test job stopped: %s", e.getMessage());
			failed++;
		} catch (Exception e) { 
			AppLogger.e(this, e, "Test job stopped: %s", e.getMessage());
			if (ex == null) error++;
		} finally {
			AppLogger.i(this, "Test result: total %d, %d success, %d failed, %d error (spent: %f)", 
					mConfig.actions.size(), success, failed, error, totalSpent);
		}
		
		AppLogger.i(this, "Done");
		return mVariables;
	}

	private void printVariables(Map<String, String> variables) {
		Iterator<Entry<String, String>> iterator = variables.entrySet().iterator();
		AppLogger.i(this, "----print variables----");
		while (iterator.hasNext())
		{
			Entry<String, String> e = iterator.next();
			AppLogger.i(this, "%s=%s", e.getKey(), e.getValue());
		}
	}

	@Override
	public TestActionStatus[] getAllStatus() {
		return mAllStatus;
	}
	
	public int getSuccess()
	{
		return success;
	}
	
	public int getFailure()
	{
		return failed;
	}
	
	public int getErrors()
	{
		return error;
	}

	public static class ActionFailedException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public ActionFailedException() {
			super();
		}

		public ActionFailedException(String message, Throwable cause,
				boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public ActionFailedException(String message, Throwable cause) {
			super(message, cause);
		}

		public ActionFailedException(String message) {
			super(message);
		}

		public ActionFailedException(Throwable cause) {
			super(cause);
		}
		
	}
}
