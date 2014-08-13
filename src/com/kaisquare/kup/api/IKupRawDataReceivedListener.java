package com.kaisquare.kup.api;

/**
 * The listener is to get raw data while the request is receiving data from server
 */
public interface IKupRawDataReceivedListener {
	/**
	 * This function will be called during receiving data from server
	 * @param request the original request
	 * @param data received data
	 * @param size the size of received data
	 * @return if the raw data has handled by other procedure, just return true and the request will ignore the data 
	 */
	boolean onKupRawDataRead(KupRequest request, byte[] data, int size);
}
