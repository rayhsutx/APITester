package com.kaisquare.kainode.tester;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;

import com.kaisquare.kainode.tester.jobs.TestJob;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class APITester {

	public static final String VERSION = "1.2";
	public static final String TAG = "Main";
	
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
		String pathTestCase = Configuration.getConfig().getConfigValue(Configuration.TEST_CASE);
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
			
			timeStart = System.nanoTime();
			runTest(fileList, variables);
			timeEnd = System.nanoTime();
			
			long spent = timeEnd - timeStart;
			
			String timeDiff = "" + (spent / 1000000) + "ms";
			
			AppLogger.i(TAG, "Total time spent: %s", timeDiff);
			
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
	
	private static Map<String, List<String>> runTest(Iterable<File> files, VariableCollection variables)
	{
		Configuration config = Configuration.getConfig();
		boolean breakLoop = false;
		ArrayList<String> passed = new ArrayList<String>();
		ArrayList<String> failed = new ArrayList<String>();
		ArrayList<String> untested = new ArrayList<String>();
		
		do
		{
			boolean skip = false;
			for (File file : files)
			{
				boolean jobSuccess = false;				
				if (!skip)
				{
					AppLogger.i(TAG, "Starting test from %s", file.getName());
					TestJob tester;
					try {						
						tester = new TestJob(file.getAbsolutePath(), variables);
						variables = tester.doTest();
						jobSuccess = (tester.getErrors() == 0 && tester.getFailure() == 0);
						
						if (jobSuccess)
							passed.add(file.getName());
						else
						{
							AppLogger.w("", "TestJob '%s' not pass", file.getName());
							failed.add(file.getName());
						}
							
					} catch (Exception e) {
						AppLogger.e("", e, "failed to run test '%s'", file);
						failed.add(file.getName());
					} finally {
					}
				}
				else
					untested.add(file.getName());
				
				skip = !config.hasConfig(Configuration.IGNORE_FAIL) && !jobSuccess;
			}
		} while (!breakLoop && config.hasConfig(Configuration.REPEAT) && !APITester.isQuitted());
		
		String[] passedResults = new String[passed.size()];
		String[] failedResults = new String[failed.size()];
		String[] untestedResults = new String[untested.size()];
		
		passedResults = passed.toArray(passedResults);
		failedResults = failed.toArray(failedResults);
		untestedResults = untested.toArray(untestedResults);
		
		HashMap<String, List<String>> results = new HashMap<String, List<String>>();
		results.put("Passed", passed);
		results.put("Failed", failed);
		results.put("Untested", untested);

		return results;
	}
}
