package com.kaisquare.kainode.tester;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
			int runTimes = 0;
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
							TestStatistics s = new TestStatistics("T-" + numberOfRuns + "-" + i);
							runTest(s, f, v);
							
							return s;
						}
					}));
				}
				
				for (Future<TestStatistics> future : futures)
				{
					try {
						statistics.addStatistics(future.get());
					} catch (InterruptedException | ExecutionException e) {
						AppLogger.w(TAG, e, "");
					}
				}
				futures.clear();
				
				duration = Math.round((System.currentTimeMillis() - startedTime) / 1000 * 100) / 100;
				AppLogger.d(TAG, "******** total duration: %s", convertDuration(duration));
			} while (!APITester.isQuitted() && (loop == -1 || ++runTimes < loop || duration < totalDuration));
			timeEnd = System.nanoTime();
			
			long spent = timeEnd - timeStart;
			String timeDiff = "" + (spent / 1000000) + "ms";
			AppLogger.i(TAG, "Total time spent: %s", timeDiff);
			
			AppLogger.i(TAG, "Total: times=%d (threads=%d) Pass: jobs=%d, actions=%d, Failed: jobs=%d, actions=%d",
					runTimes,
					threads,
					statistics.getNumberOfFiles(TestStatistics.RESULT_PASSED),
					statistics.getNumberOfActions(TestStatistics.RESULT_PASSED),
					statistics.getNumberOfFiles(TestStatistics.RESULT_FAILED),
					statistics.getNumberOfActions(TestStatistics.RESULT_FAILED));
			
			System.exit(0);
		}else{
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
	
	private static void runTest(TestStatistics statistics, Iterable<File> files, VariableCollection variables)
	{
		Configuration config = Configuration.getConfig();
		ArrayList<String> passed = new ArrayList<String>();
		ArrayList<String> failed = new ArrayList<String>();
		ArrayList<String> untested = new ArrayList<String>();
		String prefixName = Thread.currentThread().getId() + ":";
		
		boolean skip = false;
		for (File file : files)
		{
			boolean jobSuccess = false;				
			if (!skip)
			{
				AppLogger.i(TAG, "Starting test from %s", file.getName());
				TestJob tester = null;
				try {						
					tester = new TestJob(file.getAbsolutePath(), variables);
					variables = tester.doTest();
					jobSuccess = (tester.getErrors() == 0 && tester.getFailure() == 0);
					
					if (jobSuccess)
						passed.add(prefixName + file.getName());
					else
					{
						AppLogger.w("", "TestJob '%s' not pass", file.getName());
						failed.add(prefixName + file.getName());
					}
						
				} catch (Exception e) {
					AppLogger.e("", e, "failed to run test '%s'", file);
					failed.add(prefixName + file.getName());
				} finally {
					if (tester != null)
					{
						statistics.increaseResultNumbers(TestStatistics.RESULT_PASSED, tester.getSuccess());
						statistics.increaseResultNumbers(TestStatistics.RESULT_FAILED, tester.getFailure());
					}
				}
			}
			else
				untested.add(prefixName + file.getName());
			
			skip = !config.hasConfig(Configuration.IGNORE_FAIL) && !jobSuccess;
		}
		
		statistics.addResultFiles(TestStatistics.RESULT_PASSED, passed);
		statistics.addResultFiles(TestStatistics.RESULT_FAILED, failed);
		statistics.addResultFiles(TestStatistics.RESULT_UNTESTED, untested);
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
