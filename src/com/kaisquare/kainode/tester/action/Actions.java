package com.kaisquare.kainode.tester.action;

public final class Actions {
	
	public static final String ACTION_HTTP = "http";
	public static final String ACTION_PRINT = "print";
	public static final String ACTION_ARGUMENT_DELIVERY = "argument";
	public static final String ACTION_SHELL = "shell";
	public static final String ACTION_KAISYNC = "kaisync";
	
	private Actions() {}
	
	public static IAction create(String action) throws ActionNotFoundException
	{
		switch (action)
		{
		case ACTION_HTTP:
			return new HttpRequestAction();
		case ACTION_PRINT:
			return new PrintTestAction();
		case ACTION_ARGUMENT_DELIVERY:
			return new ArgumentDeliveryAction();
		case ACTION_SHELL:
			return new ShellExecuteAction();
		case ACTION_KAISYNC:
			return new KAISyncAction();

		default:
			throw new ActionNotFoundException("action '" + action + "' not found");
		}
	}

	public static class ActionNotFoundException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public ActionNotFoundException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public ActionNotFoundException(String message, Throwable cause,
				boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
			// TODO Auto-generated constructor stub
		}

		public ActionNotFoundException(String message, Throwable cause) {
			super(message, cause);
			// TODO Auto-generated constructor stub
		}

		public ActionNotFoundException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}

		public ActionNotFoundException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}
	}
}
