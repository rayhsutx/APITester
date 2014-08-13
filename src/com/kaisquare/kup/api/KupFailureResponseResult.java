package com.kaisquare.kup.api;

public class KupFailureResponseResult extends KupResponseResult {

	public KupFailureResponseResult(int status, String error) {
		result = "failed";
		reason = error;
	}

}
