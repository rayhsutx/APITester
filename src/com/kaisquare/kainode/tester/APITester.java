package com.kaisquare.kainode.tester;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.w3c.dom.Element;

import com.kaisquare.kainode.tester.jobs.TestJob;
import com.kaisquare.kainode.tester.jobs.XMLBuilder;
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
		
		DateTime now;
		long timeStart;
		long timeEnd;
		XMLBuilder xmlBuilder = new XMLBuilder();
		
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
		
		if (!Utils.isStringEmpty(pathTestCase))
		{
			createXmlElementNode(xmlBuilder, "commandUsed", new String[] { "java -jar APITester " + joinString(Arrays.asList(args), " ") }, "");

			Map<String, String> dataInput = null;
			if (!Utils.isStringEmpty(dataVariables))
			{
				dataInput = new HashMap<String, String>();
				String[] data = dataVariables.split("\\,");
				for(String d : data){
					String[] keyvalue = d.split("\\:|\\=");
					if(keyvalue.length < 2){
						dataInput.put(keyvalue[0], "");
					}
					else				
						dataInput.put(keyvalue[0], keyvalue[1]);
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
			
			now = DateTime.now();
			timeStart = System.nanoTime();
			createXmlElementNode(xmlBuilder, "timeStart", new String[]{ now.toString("yyyy/MM/dd HH:mm:ss") });
			
			Map<String, List<String>> results = runTest(xmlBuilder, fileList, dataInput);
			
			now = DateTime.now();
			timeEnd = System.nanoTime();
			long spent = timeEnd - timeStart;
			
			String timeDiff = "" + (spent / 1000000) + "ms";
			createXmlElementNode(xmlBuilder, "timeDifference", new String[]{ timeDiff });
			createXmlElementNode(xmlBuilder, "timeEnd", new String[]{ now.toString("yyyy/MM/dd HH:mm:ss") });
			
			printResults(xmlBuilder, results);
			xmlBuilder.convertXML();
			
			System.exit(0);
		}else{
			Configuration.usage();
			System.exit(1);
		}
		
	}

	private static String joinString(Iterable<String> arr, String delimeter) {
		StringBuilder sb = new StringBuilder();
		for (String s : arr)
		{
			if (sb.length() > 0)
				sb.append(delimeter);
			sb.append(s);
		}
		
		return sb.toString();
	}

	private static void loadConfiguration(String[] args) {
		Configuration config = Configuration.getConfig();
		Map<String, String> configs = Configuration.load();
		CommandArgs cmdArgs = new CommandArgs(args);
		configs.putAll(cmdArgs.getArguments());
		config.setConfigs(configs);
	}
	
	private static Map<String, List<String>> runTest(XMLBuilder xmlBuilder, Iterable<File> files, Map<String, String> defaultVariables)
	{
		Configuration config = Configuration.getConfig();
		boolean breakLoop = false;
		Element testType = xmlBuilder.createChildElement("test");
		ArrayList<String> passed = new ArrayList<String>();
		ArrayList<String> failed = new ArrayList<String>();
		ArrayList<String> untested = new ArrayList<String>();
		
		do
		{
			boolean skip = false;
			Map<String, String> variables = null;
			for (File file : files)
			{
				boolean jobSuccess = false;
				Element fileElement = xmlBuilder.createChildElement("file");
				HashMap<String, String> name = new HashMap<String, String>();
				name.put("name", file.getName());
				fileElement = xmlBuilder.writeAttributes(fileElement, name);
				
				if (!skip)
				{
					AppLogger.i(TAG, "Starting test from %s", file.getName());
					TestJob tester;
					try {
						
						if(defaultVariables != null)
							variables = defaultVariables;
						else
							variables = new HashMap<String, String>();
						
						tester = new TestJob(xmlBuilder, file.getAbsolutePath(), variables);
						variables = tester.doTest(fileElement);
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
				
				Element testStatusElement = xmlBuilder.createChildElement("TestStatus");
				testStatusElement = xmlBuilder.writeContent(testStatusElement, new String[] { 
						skip ? "untested" : jobSuccess ? "passed" : "failed" });
				xmlBuilder.writeElements(fileElement, testStatusElement);
				xmlBuilder.writeElements(testType, fileElement);
				xmlBuilder.writeToRoot(testType);
				
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
	
	private static void printResults(XMLBuilder xmlBuilder, Map<String, List<String>> results){
		Element resultSum = xmlBuilder.createChildElement("results");
		
		for(String key : results.keySet()){
			List<String> result = results.get(key);
			
			Element currResult = xmlBuilder.createChildElement(key);
			Element data = xmlBuilder.createChildElement("data");
			data = xmlBuilder.writeContent(data, new String[] { joinString(result, ", ") });
			Element count = xmlBuilder.createChildElement("count");
			count= xmlBuilder.writeContent(count, new String[]{ Integer.toString(result.size()) });
			xmlBuilder.writeElements(currResult, count);
			xmlBuilder.writeElements(currResult, data);
			xmlBuilder.writeElements(resultSum, currResult);
		}
		
		xmlBuilder.writeToRoot(resultSum);
	}
	
	private static void createXmlElementNode(XMLBuilder xmlBuilder, String childName, String[] content)
	{
		createXmlElementNode(xmlBuilder, childName, content, ",");
	}
	
	private static void createXmlElementNode(XMLBuilder xmlBuilder, String childName, String[] content, String delimeter)
	{
		Element element = xmlBuilder.createChildElement(childName);
		element = xmlBuilder.writeContent(element, content, delimeter);
		xmlBuilder.writeToRoot(element);
	}
}
