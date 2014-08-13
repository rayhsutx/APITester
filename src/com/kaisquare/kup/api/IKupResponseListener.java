package com.kaisquare.kup.api;

/**
 * A listener for handling response from remote server
 */
public interface IKupResponseListener {
	/**
	 * @param api which API of the response, refer to {@link KupHandler} to get entire API
	 * @param result the result of the request
	 */
	void onKupResponse(String api, KupResponseResult result);
}
