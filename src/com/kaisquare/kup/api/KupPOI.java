package com.kaisquare.kup.api;

public class KupPOI {
	
	public static final String TYPE_LANDMARK = "landmark";
	public static final String TYPE_BUS_STOP = "bus-stop";
	public static final String TYPE_GAS_STATION = "gas-station";
	public static final String TYPE_WIFI_ACCESS = "wifi-access";
	
	public String name;
	public String type;
	public String description;
	public String address;
	public double latitude;
	public double longitude;
	public int bucketId;
}
