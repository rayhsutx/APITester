package com.kaisquare.kainode.tester.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.kaisquare.kainode.tester.ITester;
import com.kaisquare.kainode.tester.action.ActionResult;
import com.kaisquare.kainode.tester.action.Actions;
import com.kaisquare.kainode.tester.action.RequestAction;
import com.kaisquare.kainode.tester.action.Actions.ActionNotFoundException;
import com.kaisquare.kainode.tester.action.TestActionStatus;
import com.kaisquare.kainode.tester.jobs.JobConfiguration.JobActionConfiguration;
import com.kaisquare.utils.AppLogger;

public class TestJob implements ITester {
	
	private JobConfiguration mConfig;
	private TestActionStatus[] mAllStatus;

	public TestJob(String jobFile) throws IOException
	{
		Path path = Paths.get(jobFile);
		BufferedReader reader = null;
		
		reader = Files.newBufferedReader(path, Charset.forName("utf8"));
		mConfig = new Gson().fromJson(reader, JobConfiguration.class);
		reader.close();
	}

	@Override
	public void doTest() throws Exception {
		ActionResult result = null;

		int success = 0, failed = 0, error = 0;
		boolean retrying = false;
		try {
			if (mConfig.actions.size() > 0)
			{
				mAllStatus = new TestActionStatus[mConfig.actions.size()];
				int n = 0, retry = 0;
				retrying = false;
				for (JobActionConfiguration act : mConfig.actions)
				{
					RequestAction action = (RequestAction)Actions.create(act.type);
					if (result != null)
						action.setVariables(result.getVariables());
					
					for (;;) {
						if (act.delay > 0)
						{
							AppLogger.i(this, "delay starting action '%s' in %d ms", act.name, act.delay);
							Thread.sleep(act.delay);
						}
						
						try {
							AppLogger.i(this, ">>>>>>>>>> Starting Action '%s'... <<<<<<<<<<", act.name);
							result = action.submit(act.config);
							mAllStatus[n] = result.getStatus();
							AppLogger.i(this, ">>>>>>>>>> Action '%s'...%s <<<<<<<<<<", act.name, result.getStatus());
							
							if (!act.ignoreError && result.getStatus() != TestActionStatus.Ok)
							{
								if (act.retry > retry)
								{
									retry++;
									retrying = true;
									AppLogger.i(this, "retry action '%s' %d/%d", act.name, retry, act.retry);
									continue;
								}
								throw new ActionFailedException("action '" + act.name + "' failed");
							}
						} catch (Exception e) {
							if (!act.ignoreError) throw e;
						}
						
						break;
					}
					
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
			error++;
		} finally {
			AppLogger.i(this, "Test result: total %d, %d success, %d failed, %d error", 
					mConfig.actions.size(), success, failed, error);
		}
		
		AppLogger.i(this, "Done");
	}

	@Override
	public TestActionStatus[] getAllStatus() {
		return mAllStatus;
	}

	public static class ActionFailedException extends Exception
	{

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
