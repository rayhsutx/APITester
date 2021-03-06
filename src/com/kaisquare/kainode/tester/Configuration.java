package com.kaisquare.kainode.tester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public final class Configuration {
	
	public static final String IGNORE_FAIL = "--ignore-fail";
	public static final String LOOP = "--loop";
	public static final String JOBS = "--jobs";
	public static final String VARIABLES = "--params";
	public static final String THREADS = "--threads";
	public static final String DURATION = "--duration";
	public static final String OUTPUT_JSON = "--tojson";

	public static void usage()
	{
		System.out.println();
		System.out.println("KAI Node Tester " + APITester.VERSION);
		System.out.println("Usage:");
		System.out.printf("%s   \t%s\n", JOBS, "test case of file should be (/test/,/test/case.json,/test/case2.json)");
		System.out.printf("%s  \t%s\n", VARIABLES, "variables for the test case/suite (key1:value1,key2:value2)");
		System.out.printf("%s   \t%s\n", IGNORE_FAIL, "ignore jobs if it's failed to the job");
		System.out.printf("%s   \t%s\n", LOOP, "repeat all the jobs");
		System.out.printf("%s   \t%s\n", THREADS, "how many threads of a job");
		System.out.printf("%s   \t%s\n", DURATION, "run the job in a period of time");
		System.out.printf("%s   \t%s\n", OUTPUT_JSON, "Write the result into a json file");		
		System.out.println();
	}
	
	private static final Configuration mConfig = new Configuration();
	
	private Map<String, String> mConfigs;
	private static final String CONFIG_FILE = "nodetester.conf";
	
	private Configuration() 
	{
	}
	
	void setConfigs(Map<String, String> config)
	{
		mConfigs = new TreeMap<String, String>(config);
	}
	
	Map<String, String> getAllConfigs()
	{
		return mConfigs;
	}
	
	public String getConfigValue(String key)
	{
		return mConfigs.get(key);
	}
	
	public int getIntegerValue(String name)
	{
		return getIntegerValue(name, 0);
	}
	
	public int getIntegerValue(String name, int defaultValue)
	{
		String value = mConfigs.get(name);
		
		try {
			return value != null ? Integer.parseInt(value) : defaultValue;
		} catch (NumberFormatException e) {
			AppLogger.e(this, "invalid '" + name + "' value: " + value);
			return defaultValue;
		}
	}
	
	public boolean hasConfig(String key)
	{
		return mConfigs.containsKey(key);
	}
	
	public void save()
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(CONFIG_FILE))));
			
			synchronized (mConfigs)
			{
				Iterator<Entry<String, String>> iterator = mConfigs.entrySet().iterator();
				while (iterator.hasNext())
				{
					Entry<String, String> entry = iterator.next();
					writer.write(String.format("%s=%s", 
							new String(entry.getKey().substring(2)),
							entry.getValue()
					));
					writer.newLine();
				}
			}
		} catch (Exception e) {
			AppLogger.e(this, e, "");
		} finally {
			if (writer != null)
			{
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {}
			}
		}
	}
	
	public static Configuration getConfig()
	{
		return mConfig;
	}
	
	public static Map<String, String> load()
	{
		final String TAG = "Configuration";
		
		Map<String, String> configs = new HashMap<String, String>();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new InputStreamReader(
							new FileInputStream(new File(CONFIG_FILE))));
			
			String line;
			while (reader.ready() && (line = reader.readLine()) != null)
			{
				String trimmedline = line.trim();
				
				if (Utils.isStringEmpty(trimmedline) || trimmedline.startsWith("#") || 
						(trimmedline.startsWith("[") && trimmedline.endsWith("]")))
					continue;
				else
				{
					String[] config = trimmedline.split("=", 2);
					if (config.length == 2)
					{
						String key = "--" + config[0].trim();
						configs.put(key, config[1].trim());
					}
					else
						AppLogger.d(TAG, "could not parse config: " + config[0]);
				}
			}
		} catch (FileNotFoundException e) {
			AppLogger.d(TAG, "could not find config file.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}
		
		return configs;
	}
}
