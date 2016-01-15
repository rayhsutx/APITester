package com.kaisquare.kainode.tester;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Element;

import com.kaisquare.kainode.tester.jobs.TestJob;
import com.kaisquare.kainode.tester.jobs.XMLBuilder;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class APITester {

	public static final String VERSION = "1.1";
	private static volatile boolean quitted;
	public static ArrayList<String> passed = new ArrayList<String>();
	public static ArrayList<String> failed = new ArrayList<String>();
	public static ArrayList<String> untested = new ArrayList<String>();	
	public static HashMap<String, String[]> results = new HashMap<String, String[]>();
	public static HashMap<String, String> dataInput = new HashMap<String, String>();
	public static boolean dataIn = false;
	private static Date timeStart;
	private static Date timeEnd;
	public static XMLBuilder xmlBuilder;
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
    	
		
//		
//		String value = Configuration.getConfig().getConfigValue(Configuration.JOB_FILE);
//
//		if (Utils.isStringEmpty(value))
//		{
//			Configuration.usage();
//			System.exit(-1);
//		}
//		
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			@Override
//			public void run()
//			{
//				quitted = true;
//			}
//		});
//		
//		Configuration config = Configuration.getConfig();
//		String[] files = value.split("\\,");
//		AppLogger.i("", "Items  %s", files );
		
		
		loadConfiguration(args);
		xmlBuilder = new XMLBuilder();
		timeStart = new Date();
		Element timestamp = xmlBuilder.createChildElement("timeStart");
		timestamp = xmlBuilder.writeContent(timestamp, new String[]{ new Timestamp(timeStart.getTime()).toString()});
		xmlBuilder.writeToRoot(timestamp);
		ArrayList<String> fileList = new ArrayList<String>(); 
		String pathTestSuite = Configuration.getConfig().getConfigValue(Configuration.TEST_SUITE);
		String pathTestCase = Configuration.getConfig().getConfigValue(Configuration.TEST_CASE);
		String dataVariables = Configuration.getConfig().getConfigValue(Configuration.VARIABLES);
		System.out.println(dataVariables);
		String[] files = null;
		if(!Utils.isStringEmpty(pathTestSuite) || !Utils.isStringEmpty(pathTestCase)){
			
			if(!Utils.isStringEmpty(dataVariables)){
				String[] data = dataVariables.split("\\,");
				AppLogger.i("", "Data found", "");
				for(String d : data){
					
					String[] keyvalue = d.split("\\:");
					dataInput.put(keyvalue[0], keyvalue[1]);
					
				}
				dataIn = true;
			}
			
			
			for(int i = 0; i < args.length; i+= 2){
				if (args[i].equals("--test-suite")){
					
					File dir = new File(pathTestSuite);
					File[] directoryListing = dir.listFiles();
					for(File child : directoryListing){
						String[] fileNameArray = child.getName().split("\\.");
						
						if(fileNameArray[1].equals("json")){
							fileList.add(pathTestSuite + child.getName());
							AppLogger.i("", "Added new file : %s", child.getName());
						}
					}
					
					files = new String[fileList.size()];
					files = fileList.toArray(files);
					Configuration config = Configuration.getConfig();
					runTest(files, config, "suite");
					
				}else if(args[i].equals("--test-case")){
					
					Configuration config = Configuration.getConfig();
					files = pathTestCase.split("\\,");
					AppLogger.i("", "Items  %s", files );
					runTest(files, config, "case");
				}
			}
			
		}else{
			Configuration.usage();
			System.exit(-1);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				quitted = true;
			}
		});
		timeEnd = new Date();
		long difference = timeDifference(timeStart.getTime(), timeEnd.getTime());
		
		Element timeDifference = xmlBuilder.createChildElement("timeDifference");
		String timeDiff = "" + difference + "ms";
		timeDifference = xmlBuilder.writeContent(timeDifference, new String[]{timeDiff});
		
		xmlBuilder.writeToRoot(timeDifference);
		
		Element timeEnded = xmlBuilder.createChildElement("timeEnd");
		timeEnded = xmlBuilder.writeContent(timeEnded, new String[]{new Timestamp(timeEnd.getTime()).toString()});
		xmlBuilder.writeToRoot(timeEnded);
		
		Element commandUsed = xmlBuilder.createChildElement("commandUsed");
		commandUsed = xmlBuilder.writeCommands(commandUsed, new String[]{"java -jar APITester "});
		commandUsed = xmlBuilder.writeCommands(commandUsed, args);
		xmlBuilder.writeToRoot(commandUsed);
		
		printResults(results);
		Configuration.addConfigs();
		xmlBuilder.saveXML(xmlBuilder.document, "testXML.xml");
		xmlBuilder.convertXML();
		
	}
//		do
//		{
//			Map<String, String> variables = null;
//			for (String file : fileList)
//			{
//				AppLogger.i("", "\nStarting test from %s", file.trim());
//				TestJob tester;
//				try {
//					tester = new TestJob(file.trim(), variables);
//					variables = tester.doTest();
//					if (!config.hasConfig(Configuration.IGNORE_FAIL) && (tester.getErrors() > 0 || tester.getFailure() > 0))
//					{
//						AppLogger.w("", "TestJob '%s' not pass", file);
//						breakLoop = true;
//						break;
//					}
//				} catch (Exception e) {
//					AppLogger.e("", e, "failed to run test '%s'", file);
//					breakLoop = true;
//					break;
//				}
//			}
//		} while (!breakLoop && config.hasConfig(Configuration.REPEAT) && !KaiNodeTester.isQuitted());
//		
//	}

	private static void loadConfiguration(String[] args) {
		Configuration config = Configuration.getConfig();
		Map<String, String> configs = Configuration.load();
		CommandArgs cmdArgs = new CommandArgs(args);
		configs.putAll(cmdArgs.getArguments());
		config.setConfigs(configs);
	}
	
	private static void runTest(String[] files, Configuration config, String type){
		
		boolean dataAdded = false;
		boolean breakLoop = false;
		Element testType = xmlBuilder.createChildElement("test");
		Element testTypeName = xmlBuilder.createChildElement("name");
		testTypeName = xmlBuilder.writeContent(testTypeName, new String[]{type});
		xmlBuilder.writeElements(testType, testTypeName);
		do
		{
			Map<String, String> variables = null;
			int i = 0;
			for (; i < files.length; i ++)
			{
				String file = files[i];
				String[] fileName = file.split("\\/");
				Element fileElement = xmlBuilder.createChildElement("file");
				HashMap<String, String> name = new HashMap<String, String>();
				name.put("name", fileName[fileName.length-1]);
				fileElement = xmlBuilder.writeAttributes(fileElement, name);
				AppLogger.i("", "Starting test from %s", file.trim());
				TestJob tester;
				try {
					
					if(!dataInput.isEmpty()){
						variables = dataInput;
					}
					else
						variables = new HashMap<String, String>();
					tester = new TestJob(file.trim(), variables);
					variables = tester.doTest(fileElement);
					if (!config.hasConfig(Configuration.IGNORE_FAIL) && (tester.getErrors() > 0 || tester.getFailure() > 0))
					{
						if(type.equals("suite")){
							AppLogger.w("", "TestJob '%s' not pass", file);
							AppLogger.i("", "Test Suite is Ending");
							failed.add(file);
							breakLoop = true;
							i ++;
							Element testStatusElement = xmlBuilder.createChildElement("TestStatus");
							testStatusElement = xmlBuilder.writeContent(testStatusElement, new String[]{"fail"});
							xmlBuilder.writeElements(fileElement, testStatusElement);
							xmlBuilder.writeElements(testType, fileElement);
							xmlBuilder.writeToRoot(testType);
							break;
						}
						else if(type.equals("case")){
							
							AppLogger.w("", "TestJob '%s' not pass", file);
							failed.add(file);
							Element testStatusElement = xmlBuilder.createChildElement("TestStatus");
							testStatusElement = xmlBuilder.writeContent(testStatusElement, new String[]{"fail"});
							xmlBuilder.writeElements(fileElement, testStatusElement);
							xmlBuilder.writeElements(testType, fileElement);
							xmlBuilder.writeToRoot(testType);
							breakLoop = true;
							continue;
						}
						
					} else{
						passed.add(file);
						Element testStatusElement = xmlBuilder.createChildElement("TestStatus");
						testStatusElement = xmlBuilder.writeContent(testStatusElement, new String[]{"pass"});
						xmlBuilder.writeElements(fileElement, testStatusElement);
						xmlBuilder.writeElements(testType, fileElement);
						xmlBuilder.writeToRoot(testType);
						
					}
				} catch (Exception e) {
					AppLogger.e("", e, "failed to run test '%s'", file);
					breakLoop = true;
					i++;
					failed.add(file);
					Element testStatusElement = xmlBuilder.createChildElement("TestStatus");
					testStatusElement = xmlBuilder.writeContent(testStatusElement, new String[]{"fail"});
					xmlBuilder.writeElements(fileElement, testStatusElement);
					xmlBuilder.writeElements(testType, fileElement);
					xmlBuilder.writeToRoot(testType);
					break;
			 		
				}
			}
			for(;i < files.length; i ++){
				untested.add(files[i]);
				String[] fileName = files[i].split("\\/");
				Element fileElement = xmlBuilder.createChildElement("file");
				HashMap<String, String> name = new HashMap<String, String>();
				name.put("name", fileName[fileName.length-1]);
				fileElement = xmlBuilder.writeAttributes(fileElement, name);
				Element testStatusElement = xmlBuilder.createChildElement("TestStatus");
				testStatusElement = xmlBuilder.writeContent(testStatusElement, new String[]{"untested"});
				xmlBuilder.writeElements(fileElement, testStatusElement);
				xmlBuilder.writeElements(testType, fileElement);
				xmlBuilder.writeToRoot(testType);
			}
			
			
		} while (!breakLoop && config.hasConfig(Configuration.REPEAT) && !APITester.isQuitted());
		
		String[] passedResults = new String[passed.size()];
		String[] failedResults = new String[failed.size()];
		String[] untestedResults = new String[untested.size()];
		
		passedResults = passed.toArray(passedResults);
		failedResults = failed.toArray(failedResults);
		untestedResults = untested.toArray(untestedResults);
		

		results.put("Passed", passedResults);
		results.put("Failed", failedResults);
		results.put("Untested", untestedResults);

		
		return;
		
	}
	
	private static void printResults(HashMap<String, String[]> results){
		Element resultSum = xmlBuilder.createChildElement("results");
		System.out.println();
		AppLogger.i("", "-------- Report Generation -------");
		for(String key : results.keySet()){
			String[] result = results.get(key);
			if(result.length == 0){
				result = new String[1];
				result[0] = "None";
			}
			
			AppLogger.i("", key + ": %s", Arrays.toString(result));
			Element currResult = xmlBuilder.createChildElement(key);
			Element data = xmlBuilder.createChildElement("data");
			data = xmlBuilder.writeContent(data, result);
			Element count = xmlBuilder.createChildElement("count");
			count= xmlBuilder.writeContent(count, new String[]{Integer.toString(results.get(key).length)});
			xmlBuilder.writeElements(currResult, count);
			xmlBuilder.writeElements(currResult, data);
			xmlBuilder.writeElements(resultSum, currResult);
		}
		
		AppLogger.i("", "------ End of Report --------");
		System.out.println();
		xmlBuilder.writeToRoot(resultSum);
		return;
	}
	
	private static String[] combineArray(String[] one, String[] two){
		
		String[] array = new String[one.length + two.length];
		System.arraycopy(one, 0, array, 0, one.length);
		System.arraycopy(two, 0, array, one.length, two.length);
		
		return array;
	}
	
	private static long timeDifference(long start, long end){
		
		long difference = end - start;
		
		return difference;
	}
	
	
	

}
