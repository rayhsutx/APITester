package com.kaisquare.kup.api;

import java.util.List;

/**
 * KUP bucket object
 */
public class KupBucket {
	/**
	 * Bucket name
	 */
	public String name;
	/**
	 * Bucket path
	 */
	public String path;
	/**
	 * Supported services
	 */
	public List<KupService> services;
}
