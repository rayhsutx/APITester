package com.kaisquare.kup.api;

import java.util.ArrayList;

public class KupNode
{
	public String name;
	public String cloudCoreDeviceId;
	public String cloudPlatformDeviceId;
	//public KupNodeCamera cameras;
	public ArrayList<KupNodeCamera>  cameras = new ArrayList<KupNodeCamera>();
	
	
}
