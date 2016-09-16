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

import com.kaisquare.gson.DefaultGsonBuilder;
import com.kaisquare.kainode.tester.APITester;
import com.kaisquare.kainode.tester.ITester;
import com.kaisquare.kainode.tester.VariableCollection;
import com.kaisquare.kainode.tester.action.Actions;
import com.kaisquare.kainode.tester.action.Actions.ActionNotFoundException;
import com.kaisquare.kainode.tester.action.RequestAction;
import com.kaisquare.kainode.tester.action.TestActionStatus;
import com.kaisquare.kainode.tester.action.result.ActionResult;
import com.kaisquare.kainode.tester.jobs.JobConfiguration.JobActionConfiguration;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class TestJob implements ITester {
	
	private String jobName;
	private JobConfiguration config;
	private TestActionStatus[] allStatus;
	private VariableCollection variables;
	private int success = 0, failed = 0, error = 0;

	public TestJob(String jobFile, VariableCollection variables) throws IOException
	{
		Path path = Paths.get(jobFile);
		jobName = path.toFile().getName();
		BufferedReader reader = null;
		
		try {
			reader = Files.newBufferedReader(path, Charset.forName("utf8"));
			config = DefaultGsonBuilder.create().fromJson(reader, JobConfiguration.class);
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
		
		this.variables = variables;
	}

	@Override
	public VariableCollection doTest() throws Exception {
		
		ActionResult result = null;
		double totalSpent = 0;
		Exception ex = null;
		int loop = 1;
		
		try {
			if (config.getActions().size() > 0)
			{
				long startedTime = System.currentTimeMillis();
				float duration = 0;
				do {
					AppLogger.i(this, "'%s' loop %d/%d", jobName, loop, config.getLoop());
					allStatus = new TestActionStatus[config.getActions().size()];
					int n = 0, retry = 0;
					for (JobActionConfiguration act : config.getActions())
					{					
						if (APITester.isQuitted())
							break;
						
						RequestAction action = (RequestAction)Actions.create(act.getType());
						action.setVariables(variables);
						
						int repeat = 0;
						long start, end;
						double spent;
						for (;;) {
							if (act.getDelay() > 0)
							{
								AppLogger.i(this, "delay starting action '%s' in %d ms", act.getName(), act.getDelay());
								Thread.sleep(act.getDelay());
							}
							else
								act.setDelay(config.getDefaultDelay());
							
							try {
								ex = null;
								AppLogger.i(this, "\n>>>>>>>>>> Starting Action '%s'... <<<<<<<<<< (repeat %d)", act.getName(), repeat);
								start = System.nanoTime();
								result = action.submit(act.getConfig());
								end = System.nanoTime();
								allStatus[n] = result.getStatus();
								spent = (end - start) / 1000000f;
								totalSpent += spent;
								AppLogger.i(this, ">>>>>>>>>> Action '%s'...%s <<<<<<<<<< (spent: %f ms)\n", act.getName(), result.getStatus(), spent);
								
								if (!act.isIgnoreError() && result.getStatus() != TestActionStatus.Ok)
								{
									if (act.getRetry() > retry)
									{
										retry++;
										AppLogger.i(this, "retry action '%s' %d/%d", act.getName(), retry, act.getRetry());
										continue;
									}
									printVariables(action.getVariables());
									throw new ActionFailedException(
											result.getStatus() == TestActionStatus.Error ?
													result.getReason() :
													"action '" + act.getName() + "' failed");
								}
							} catch (Exception e) {
								if (!act.isIgnoreError())
								{
									ex = e;
									break;
								}
							}
							
							if (act.getRepeat() < 0)
								continue;
							else if (act.getRepeat() > repeat)
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
						
						if (act.getPrint() != null)
						{
							AppLogger.i(this, "print variables >>>>>");
							for (String var : act.getPrint())
							{
								if (!Utils.isStringEmpty(var))
									AppLogger.i(this, "%s=%s", var, variables.get(var));
							}
							AppLogger.i(this, "<<<<<");
						}
						
						if (!act.isIgnoreError() && ex != null)
							throw ex;
					}
					
					duration = Math.round((System.currentTimeMillis() - startedTime) / 1000f * 100) / 100;
					AppLogger.d(this, "'%s' duration: %s", jobName, APITester.convertDuration(duration));
				} while (++loop < config.getLoop() || duration < config.getDuration());
			}
			else
				AppLogger.i(this, "No actions");
		} catch (ActionNotFoundException e) {
			AppLogger.e(this, "'%s' stopped: %s", jobName, e.getMessage());
			error++;
		} catch (ActionFailedException e) {
			AppLogger.e(this, "'%s' stopped: %s", jobName, e.getMessage());
			failed++;
		} catch (Exception e) { 
			AppLogger.e(this, e, "'%s' stopped: %s", jobName, e.getMessage());
			if (ex == null) error++;
		} finally {
			AppLogger.i(this, "'%s' result: total %d, %d success, %d failed, %d error (spent: %f)", 
					jobName, config.getActions().size(), success, failed, error, totalSpent);
		}
		
		AppLogger.i(this, "Done");
		return variables;
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
		return allStatus;
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
