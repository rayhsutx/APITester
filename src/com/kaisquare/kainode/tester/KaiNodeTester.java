package com.kaisquare.kainode.tester;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;

import com.kaisquare.kainode.tester.jobs.TestJob;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class KaiNodeTester {

	public static final String VERSION = "1.0";	

	public static void main(String[] args) {
		System.out.println("version " + VERSION);
		if (!Files.exists(Paths.get("log4j.properties")))
		{
			URL configURL = KaiNodeTester.class.getResource("/res/log4j.properties");
	    	PropertyConfigurator.configure(configURL);
		}
		else
			PropertyConfigurator.configure("log4j.properties");
    	
		loadConfiguration(args);
		
		String value = Configuration.getConfig().getConfigValue(Configuration.JOB_FILE);
		if (Utils.isStringEmpty(value))
		{
			Configuration.usage();
			System.exit(-1);
		}
		
		Configuration config = Configuration.getConfig();
		String[] files = value.split("\\,");
		Map<String, String> variables = null;
		for (String file : files)
		{
			AppLogger.i("", "\nStarting test from %s", file.trim());
			TestJob tester;
			try {
				tester = new TestJob(file.trim(), variables);
				variables = tester.doTest();
				if (!config.hasConfig(Configuration.IGNORE_FAIL) && (tester.getErrors() > 0 || tester.getFailure() > 0))
				{
					AppLogger.w("", "TestJob '%s' not pass", file);
					break;
				}
			} catch (Exception e) {
				AppLogger.e("", e, "failed to run test '%s'", file);
				break;
			}
		}
	}

	private static void loadConfiguration(String[] args) {
		Configuration config = Configuration.getConfig();
		Map<String, String> configs = Configuration.load();
		CommandArgs cmdArgs = new CommandArgs(args);
		configs.putAll(cmdArgs.getArguments());
		config.setConfigs(configs);
	}

}
