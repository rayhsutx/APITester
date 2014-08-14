package com.kaisquare.kainode.tester.action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kaisquare.utils.AppLogger;
import com.kaisquare.utils.Utils;

public class ShellExecuteAction extends RequestAction {
	
	private Process mProcess;
	private Timer mTimer;
	private String mPid;
	private boolean mTimeout = false;

	@Override
	public String getActionName() {
		return "shell";
	}

	@Override
	public String getActionType() {
		return Actions.ACTION_SHELL;
	}

	@Override
	public ActionResult submit(ActionConfiguration config) {
		
		if (Utils.isStringEmpty(config.command))
			throw new NullPointerException("command is empty.");
		
		ActionResult result = new EmptyActionResult(TestActionStatus.Ok);
		BufferedReader reader = null;
		BufferedWriter out = null;
		int exitValue = -1;
		String command = "";
		
		try {
			command = parseVariables(config.command);
			AppLogger.d(this, "running command: %s", command);
			ProcessBuilder pb = new ProcessBuilder(command.split(" "));
			pb.redirectErrorStream(true);
			mProcess = pb.start();
			mPid = getPID(mProcess);
			AppLogger.v(this, "pid = %s", mPid);
			
			if (config.timeout > 0)
			{
				mTimer = new Timer();
				mTimer.schedule(new TimeoutTask(), config.timeout);
			}
			
			reader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
			String line;
			
			if (!Utils.isStringEmpty(config.redirect))
			{
				Path path = Paths.get(config.redirect);
				out = Files.newBufferedWriter(path, Charset.forName("utf8"), StandardOpenOption.CREATE);
			}
			
			Map<String, String> values = new HashMap<String, String>();
			values.putAll(getVariables());
			
			ValueParser[] parsers = getValueParsers(config.values);
			int number = 0;
			while ((line = reader.readLine()) != null)
			{
				number++;
				AppLogger.v(this, "%d:%s", number, line);
				if (parsers != null)
				{
					for (ValueParser parser : parsers)
					{
						if (parser.getLineNumber() == -1 || parser.getLineNumber() == number)
						{
							String value = parser.parse(line);
							if (!Utils.isStringEmpty(value))
								values.put(parser.getName(), value);
						}
					}
				}
				if (out != null)
					out.write(line);
			}
			try {
				mProcess.waitFor();
			} catch (InterruptedException e) {}
			exitValue = mProcess.exitValue();
			
			values.put("exitvalue", Integer.toString(exitValue));
			result.putVariableAll(values);
			
			checkResult(result, config.check);
		} catch (IOException e) {
			AppLogger.e(this, e, "error executing command: %s", Utils.isStringEmpty(command) ? config.command : command);
			result.setStatus(TestActionStatus.Error);
		} finally {
			if (mTimer != null)
			{
				mTimer.cancel();
				mTimer.purge();
			}
			if (mProcess != null)
			{
				mProcess.destroy();
				mProcess = null;
			}
			if (reader != null)
			{
				try {
					reader.close();
					reader = null;
				} catch (IOException e) {}
			}
			if (out != null)
			{
				try {
					out.flush();
					out.close();
				} catch (Exception e) {}
			}
		}
		
		return result;
	}

	private ValueParser[] getValueParsers(Map<String, String> actions) throws IOException {
		ValueParser[] parsers = null;
		if (actions != null && actions.size() > 0)
		{
			parsers = new ValueParser[actions.size()];
			Iterator<Entry<String, String>> iterator = actions.entrySet().iterator();
			int n = 0;
			while (iterator.hasNext())
			{
				Entry<String, String> entry = iterator.next();
				String action = entry.getValue();
				if (Utils.isStringEmpty(action) || (!action.startsWith("re:") && !action.startsWith("shell:")))
					throw new IOException("Unknown action: " + action);
				
				String[] actionValue = action.split("\\:", 3);
				String actionBy;
				String commandOrRE;
				int lineNumber = -1;
				if (actionValue.length > 2)
				{
					actionBy = actionValue[0];
					lineNumber = Integer.parseInt(actionValue[1]);
					commandOrRE = actionValue[2];
				}
				else
				{
					actionBy = actionValue[0];
					commandOrRE = actionValue[1];
				}
					
				parsers[n] = new ValueParser(entry.getKey());
				parsers[n].setLineNumber(lineNumber);
				if (actionBy.equalsIgnoreCase("re"))
					parsers[n].setParseByRE(Pattern.compile(commandOrRE));
				else if (actionBy.equals("shell"))
					parsers[n].setParseByShell(commandOrRE);
				
				n++;
			}
		}
		
		return parsers;
	}
	
	private static String getPID(Process process)
	{
		try {
        	Class clazz = Class.forName("java.lang.UNIXProcess");
			Field pidField = clazz.getDeclaredField("pid");
			pidField.setAccessible(true);
			Object value = pidField.get(process);
			
			return String.valueOf(value);
		} catch (Throwable e) {
			AppLogger.e("getPID", e, "");
		}
		
		return "";
	}
	
	private static class ValueParser
	{
		public String varName;
		public int lineNumber;
		public Pattern pattern;
		public String shell;
		
		public ValueParser(String name)
		{
			varName = name;
		}
		
		public String getName()
		{
			return varName;
		}
		
		public void setLineNumber(int line)
		{
			lineNumber = line;
		}
		
		public int getLineNumber()
		{
			return lineNumber;
		}
		
		public void setParseByRE(Pattern pattern)
		{
			this.pattern = pattern;
		}
		
		public void setParseByShell(String shell)
		{
			this.shell = shell;
		}
		
		public String parse(String input)
		{
			StringBuilder sb = new StringBuilder();
			String value = "";
			if (pattern != null)
			{
				Matcher matcher = pattern.matcher(input);
				while (matcher.find())
				{
					int start = matcher.start();
					int end = matcher.end();
					sb.append(new String(input.substring(start, end)));
				}
				value = sb.toString();
			}
			else if (!Utils.isStringEmpty(shell))
			{
				Process p = null;
				BufferedReader reader = null;
				BufferedWriter writer = null;
				try {
//					p = new ProcessBuilder(shell.split(" ", 3)).redirectErrorStream(true).start();
					p = Runtime.getRuntime().exec(shell);
					reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
					String line;
					
					try {
						writer.write(input);
						writer.write("\n");
						writer.flush();
					} catch (IOException e) {
						AppLogger.e(this, e, "");
					} finally {
						writer.close();
					}
					while ((line = reader.readLine()) != null)
					{
						sb.append(line);
					}
					value = sb.toString();
				} catch (IOException e) {
					AppLogger.e(this, "error parsing output(%s): %s, using %s", e.getMessage(), input, shell);
				} finally {
					if (p != null)
						p.destroy();
					if (reader != null)
					{
						try {
							reader.close();
						} catch (IOException e) {}
					}
				}
				
			}
			
			return value;
		}
	}

	private class TimeoutTask extends TimerTask
	{
		@Override
		public void run() {
			AppLogger.v(this, "kill process due to timeout");
			try {
				for (int i = 0; i < 2; i++)
				{
					Runtime.getRuntime().exec("kill -2 " + mPid);
					Thread.sleep(2000);
				}
				mProcess.destroy();
			} catch (Exception e) {}
			mTimeout = true;
		}
	}
}
