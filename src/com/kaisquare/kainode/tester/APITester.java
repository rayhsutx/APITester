package com.kaisquare.kainode.tester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.PropertyConfigurator;

import com.kaisquare.gson.DefaultGsonBuilder;
import com.kaisquare.kainode.tester.TestStatistics.Job;
import com.kaisquare.kainode.tester.jobs.TestJob;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class APITester {

	public static final String VERSION = "1.3";
	public static final String TAG = "Main";
	
	private static final int DURATION_DAY = 24 * 60 * 60;
	private static final int DURATION_HOUR = 60 * 60;
	private static final int DURATION_MINUTE = 60;
	
	private static volatile boolean quitted;
	
	public static boolean isQuitted()
	{
		return quitted;
	}

	public static void main(String[] args) {
		System.out.println("version " + VERSION);
		if (!Files.exists(Paths.get("log4j.properties")))
		{
			URL configURL = APITester.class.getResource("/res/log4j.properties");
	    	PropertyConfigurator.configure(configURL);
		}
		else
			PropertyConfigurator.configure("log4j.properties");
		
		loadConfiguration(args);
		
		long timeStart;
		long timeEnd;
		
		List<File> fileList = new LinkedList<File>(); 
		String pathTestCase = Configuration.getConfig().getConfigValue(Configuration.JOBS);
		String dataVariables = Configuration.getConfig().getConfigValue(Configuration.VARIABLES);
		AppLogger.d(TAG, "variables: %s", dataVariables);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				quitted = true;
			}
		});
		
		VariableCollection variables = new VariableCollection();
		
		if (!Utils.isStringEmpty(pathTestCase))
		{
			if (!Utils.isStringEmpty(dataVariables))
			{
				String[] data = dataVariables.split("\\,");
				for(String d : data){
					String[] keyvalue = d.split("\\:|\\=");
					if(keyvalue.length < 2){
						variables.put(keyvalue[0], "");
					}
					else				
						variables.put(keyvalue[0], keyvalue[1]);
				}
			}
			
			String[] files = pathTestCase.split("\\,");
			for (String file : files)
			{
				File dir = new File(file);
				if (dir.isDirectory())
				{
					File[] directoryListing = dir.listFiles();
					for(File child : directoryListing){					
						fileList.add(child);
						AppLogger.i(TAG, "Added new test file : %s", child.getName());
					}
				}
				else if (dir.isFile())
					fileList.add(new File(file));
			}
			
			Configuration config = Configuration.getConfig();
			int loop = config.getIntegerValue(Configuration.LOOP, 1);
			int threads = config.getIntegerValue(Configuration.THREADS, 1);
			int totalDuration = config.getIntegerValue(Configuration.DURATION, 0);
			int runTimes = 1;
			int duration = 0;
			
			if (threads < 1) threads = 1;
			ExecutorService es = Executors.newFixedThreadPool(threads, new PThreadFactory("job"));
			List<Future<TestStatistics>> futures = new LinkedList<Future<TestStatistics>>();

			final TestStatistics statistics = new TestStatistics("all");
			final AtomicInteger index = new AtomicInteger(0);
			final List<File> f = fileList;
			final VariableCollection v = variables;
			
			long startedTime = System.currentTimeMillis();
			timeStart = System.nanoTime();
			
			do {
				final int numberOfRuns = runTimes;
				for (int i = 0; i < threads; i++)
				{
					futures.add(es.submit(new Callable<TestStatistics>() {
						public TestStatistics call() {
							int i = index.incrementAndGet();
							runTest("T-" + numberOfRuns + "-" + i, statistics, f, v);
							
							return statistics;
						}
					}));
				}
				
				for (Future<TestStatistics> future : futures)
				{
					try {
						future.get();
					} catch (InterruptedException | ExecutionException e) {
						AppLogger.w(TAG, e, "");
					}
				}
				futures.clear();
				
				duration = Math.round((System.currentTimeMillis() - startedTime) / 1000 * 100) / 100;
				AppLogger.d(TAG, "******** total duration: %s", convertDuration(duration));
			} while (!APITester.isQuitted() && (loop == -1 || runTimes++ < loop || duration < totalDuration));
			timeEnd = System.nanoTime();
			
			long spent = timeEnd - timeStart;
			String timeDiff = "" + (spent / 1000000) + "ms";
			AppLogger.i(TAG, "Total time spent: %s", timeDiff);
			
			AppLogger.i(TAG, "Total: times=%d (threads=%d) jobs=%d Pass: job=%d, actions=%d, Failed: jobs=%d, actions=%d",
					runTimes,
					threads,
					statistics.getNumberOfDetails(),
					statistics.getNumberOfJobResult(TestStatistics.RESULT_PASSED),
					statistics.getNumberOfDetailResult(TestStatistics.RESULT_PASSED),
					statistics.getNumberOfJobResult(TestStatistics.RESULT_FAILED),
					statistics.getNumberOfDetailResult(TestStatistics.RESULT_FAILED));
			
			String toFile = config.getConfigValue(Configuration.OUTPUT_JSON);
			if (!Utils.isStringEmpty(toFile))
			{
				BufferedWriter writer = null;
				try {
					writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(toFile))));
					DefaultGsonBuilder.create().toJson(statistics, writer);
				} catch (Exception e) {
					System.err.println("unable to write file '" + toFile + "'");
				} finally {
					if (writer != null)
					{
						try {
							writer.close();
						} catch (Exception e) {}
					}
				}
			}
			System.exit(0);
		}
		else
		{
			Configuration.usage();
			System.exit(1);
		}
		
	}

	private static void loadConfiguration(String[] args) {
		Configuration config = Configuration.getConfig();
		Map<String, String> configs = Configuration.load();
		CommandArgs cmdArgs = new CommandArgs(args);
		configs.putAll(cmdArgs.getArguments());
		config.setConfigs(configs);
	}
	
	private static void runTest(String name, TestStatistics statistics, Iterable<File> files, VariableCollection variables)
	{
		Configuration config = Configuration.getConfig();
		long tid = Thread.currentThread().getId();
		int n = 1;
		
		boolean skip = false;
		long start, end;
		for (File file : files)
		{
			TestStatistics s = new TestStatistics(name);
			boolean jobSuccess = false;				
			if (!skip)
			{
				AppLogger.i(TAG, "Starting test from %s", file.getName());
				TestJob tester = null;
				
				try {
					tester = new TestJob(file.getAbsolutePath(), variables);
					start = System.nanoTime();
					variables = tester.doTest(s);
					end = System.nanoTime();
					jobSuccess = (tester.getErrors() == 0 && tester.getFailure() == 0);
					
					if (jobSuccess)
						statistics.addResultJob(TestStatistics.RESULT_PASSED, new Job(n++, tid, file.getName(), start, end));
					else
					{
						AppLogger.w("", "TestJob '%s' not pass", file.getName());
						statistics.addResultJob(TestStatistics.RESULT_FAILED, new Job(n++, tid, file.getName(), start, end));
					}
						
				} catch (Exception e) {
					AppLogger.e("", e, "failed to run test '%s'", file);
					s.addResultJob(TestStatistics.RESULT_FAILED, new Job(n++, tid, file.getName(), 0, 0));
				} finally {
				}
			}
			else
				statistics.addResultJob(TestStatistics.RESULT_UNTESTED, new Job(n++, tid, file.getName(), 0, 0));
			
			skip = !config.hasConfig(Configuration.IGNORE_FAIL) && !jobSuccess;
			statistics.addDetail(s);
		}
	}
	
	public static String convertDuration(float duration) {
		float d = duration;
		StringBuilder sb = new StringBuilder();
		
		if (d >= DURATION_DAY)
		{
			sb.append((d / DURATION_DAY) + " d ");
			d = d % DURATION_DAY;
		}
		if (d >= DURATION_HOUR)
		{
			sb.append((d / DURATION_HOUR) + " h ");
			d = d % DURATION_HOUR;
		}
		if (d >= DURATION_MINUTE)
		{
			sb.append((d / DURATION_MINUTE) + " m ");
			d = d % DURATION_MINUTE;
		}
		sb.append(d + " s");
		
		return sb.toString();
	}
}
