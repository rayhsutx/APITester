package com.kaisquare.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class AppLogger {

	public static final int LOG_VERBOSE = 1;
	public static final int LOG_DEBUG = LOG_VERBOSE << 1;
	public static final int LOG_INFO = LOG_VERBOSE << 2;
	public static final int LOG_WARN = LOG_VERBOSE << 3;
	public static final int LOG_ERROR = LOG_VERBOSE << 4;
	
	public static final int OUTPUT_CONSOLE = 1;
	public static final int OUTPUT_FILE = OUTPUT_CONSOLE << 1;

	private static int LOG_LEVEL = LOG_VERBOSE | LOG_DEBUG | LOG_INFO
			| LOG_WARN | LOG_ERROR;
	
	private static int OUTPUT = OUTPUT_FILE;
	private static BufferedOutputStream bos;
	private static File LOG_FILE = new File("kaisync.log");
	
	private static final String WRAP_LINE = "\n";
	private static long LOG_SIZE = 104857600;
	
	public static boolean LOG4J = true;
	
	public static void setLogName(String name)
	{
		LOG_FILE = new File(name + ".log");
	}
	
	public static void setMaxFileSize(long size)
	{
		LOG_SIZE = size;
	}

	public static void setLogLevel(int level) {
		LOG_LEVEL = level;
	}
	
	public static void setLogOutput(int target)
	{
		OUTPUT = target;
	}

	public static void v(Object cls, String format, Object...args) {
		v(cls, null, format, args);
	}

	public static void i(Object cls, String format, Object...args) {
		i(cls, null, format, args);
	}

	public static void d(Object cls, String format, Object...args) {
		d(cls, null, format, args);
	}

	public static void w(Object cls, String format, Object...args) {
		w(cls, null, format, args);
	}

	public static void e(Object cls, String format, Object...args) {
		e(cls, null, format, args);
	}

	public static void v(String tag, String format, Object...args) {
		v(tag, null, format, args);
	}

	public static void i(String tag, String format, Object...args) {
		i(tag, null, format, args);
	}

	public static void d(String tag, String format, Object...args) {
		d(tag, null, format, args);
	}

	public static void w(String tag, String format, Object...args) {
		w(tag, null, format, args);
	}

	public static void e(String tag, String format, Object...args) {
		e(tag, null, format, args);
	}

	public static void v(Object cls, Throwable e, String format, Object...args) {
		v(cls.getClass().getSimpleName() == null ? cls.getClass().getName() : cls.getClass().getSimpleName(),
				e, format, args);
	}

	public static void d(Object cls, Throwable e, String format, Object...args) {
		d(cls.getClass().getSimpleName() == null ? cls.getClass().getName() : cls.getClass().getSimpleName(),
				e, format, args);
	}

	public static void i(Object cls, Throwable e, String format, Object...args) {
		i(cls.getClass().getSimpleName() == null ? cls.getClass().getName() : cls.getClass().getSimpleName(),
				e, format, args);
	}

	public static void w(Object cls, Throwable e, String format, Object...args) {
		w(cls.getClass().getSimpleName() == null ? cls.getClass().getName() : cls.getClass().getSimpleName(), 
				e, format, args);
	}

	public static void e(Object cls, Throwable e, String format, Object...args) {
		e(cls.getClass().getSimpleName() == null ? cls.getClass().getName() : cls.getClass().getSimpleName(),
				e, format, args);
	}

	public static void v(String tag, Throwable e, String format, Object...args) {
		log(tag, LOG_VERBOSE, e, format, args);
	}

	public static void d(String tag, Throwable e, String format, Object...args) {
		log(tag, LOG_DEBUG, e, format, args);
	}

	public static void i(String tag, Throwable e, String format, Object...args) {
		log(tag, LOG_INFO, e, format, args);
	}

	public static void w(String tag, Throwable e, String format, Object...args) {
		log(tag, LOG_WARN, e, format, args);
	}

	public static void e(String tag, Throwable e, String format, Object...args) {
		log(tag, LOG_ERROR, e, format, args);
	}
	
	private static void log4j(String tag, int type, Throwable e, String messageFormat, Object...args)
	{
		Logger logger = Logger.getLogger(tag);
		String message = null;
		
		try {
			if (args.length > 0)
				message = String.format(messageFormat, args);
			else
				message = messageFormat;
		} catch (Exception ex) {
			message = messageFormat;
		}
		
		switch (type)
		{
		case LOG_VERBOSE:
			if (e != null)
				logger.trace(message, e);
			else
				logger.trace(message);
			break;
		case LOG_DEBUG:
			if (e != null)
				logger.debug(message, e);
			else
				logger.debug(message);
			break;
		case LOG_INFO:
			if (e != null)
				logger.info(message, e);
			else
				logger.info(message);
			break;
		case LOG_WARN:
			if (e != null)
				logger.warn(message, e);
			else
				logger.warn(message);
			break;
		case LOG_ERROR:
			if (e != null)
				logger.error(message, e);
			else
				logger.error(message);
			break;
		}
	}

	private static void log(String tag, int type, Throwable e, String messageFormat, Object...args) {
		if (LOG4J)
		{
			log4j(tag, type, e, messageFormat, args);
			return;
		}
		String message = args.length == 0 ? messageFormat : String.format(messageFormat, args);
		if ((type & LOG_LEVEL) != 0) {
			StringBuffer sb = null;
			if (e != null) {
				sb = new StringBuffer();
				StackTraceElement[] execElements = e.getStackTrace();

				sb.append(e.toString());
				sb.append(WRAP_LINE);
				if (execElements != null) {
					for (StackTraceElement el : execElements) {
						sb.append("  at " + el.getClassName() + " in "
								+ el.getMethodName() + ":" + el.getLineNumber());
						sb.append(WRAP_LINE);
					}
				}
				if (e.getCause() != null) {
					sb.append("Caused by " + e.getCause().toString());
					sb.append(WRAP_LINE);
					StackTraceElement[] causeElements = e.getCause()
							.getStackTrace();
					for (StackTraceElement el : causeElements) {
						sb.append("  at " + el.getClassName() + " in "
								+ el.getMethodName() + ":" + el.getLineNumber());
						sb.append(WRAP_LINE);
					}
				}
			}
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String log = String.format("%s (%s):\t[%s] %s %s", format
					.format(new Date()), type == LOG_VERBOSE ? "Verbose"
							: type == LOG_DEBUG ? "Debug" : type == LOG_INFO ? "Info"
									: type == LOG_WARN ? "Warn"
											: type == LOG_ERROR ? "Error" : "Unknown",
							tag, message, e == null ? "" : sb.toString());
			
			if ((OUTPUT & OUTPUT_CONSOLE) != 0)
			{
				if (type == LOG_ERROR)
					System.err.println(log);
				else
					System.out.println(log);
			}
			if ((OUTPUT & OUTPUT_FILE) != 0)
				writeFile(log + "\n");
		}
	}
	
	private static void writeFile(String message)
	{
		if (!LOG_FILE .exists())
		{
			try {
				LOG_FILE.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (LOG_FILE.length() >= LOG_SIZE)
		{
			try {
				bos.flush();
				bos.close();
			} catch (IOException e) {
			} finally {
				bos = null;
			}
//			Utils.copyFile(LOG_FILE, new File(LOG_FILE.getName() + ".1"));
			LOG_FILE.delete();
		}
		
		if (bos == null)
		{
			try {
				bos = new BufferedOutputStream(new FileOutputStream(LOG_FILE, true));
			} catch (FileNotFoundException e) {}
		}
		
		try {
			bos.write(message.getBytes());
			bos.flush();
		} catch (IOException e) {}
	}
	
	public static void close()
	{
		if (bos != null)
		{
			try {
				bos.flush();
				bos.close();
			} catch (IOException e) {}
			
		}
	}
}
