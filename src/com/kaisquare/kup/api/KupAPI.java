package com.kaisquare.kup.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kaisquare.kaisync.utils.Utils;

/**
 * KUP API
 * it keeps global session, and do the bridge for the KUP request
 */
public final class KupAPI {

//	private static final String USER_AGENT = "KAI Square Pte. Ltd. KupMobile Client v";
	public static final String LOGIN = "login";
	public static final String USER_DEVICES = "getuserdevices";
	public static final String KEEP_SESSION_ALIVE = "keepalive";
	public static final String LIVE_VIDEO = "getlivevideourl";
	public static final String PLAYBACK_VIDEO = "getplaybackvideourl";
	public static final String EVENTS = "getevents";
	public static final String ALL_EVENTS = "getallevents";
	public static final String COMET_NOTIFICATION = "recvcometnotification";
	public static final String REGISTER_GCM_DEVICE = "registergcmdevice";
	public static final String UNREGISTER_GCM_DEVICE = "unregistergcmdevice";
	public static final String LOGOUT = "logout";
	public static final String SEND_EVENT_MESSAGE = "sendeventmessage";
	public static final String GET_EVENT_CONVERSATION = "geteventconversation";
	public static final String LIVE_LOCATION = "getlivelocation";
	public static final String HISTORICAL_LOCATION = "gethistoricallocation";
	public static final String BUCKET_POIS = "getbucketpois";
	public static final String DEVICE_MODELS = "getdevicemodels";
	public static final String KEEP_ALIVE_VIDEO = "keepalivelivevideourl";
	public static final String EVENT_BLOB_UPLOAD = "uploadeventblob";
	public static final String GET_EVENT_BLOB = "geteventblob";
	public static final String ADD_DEVICE_TO_BUCKET = "adddevicetobucket";
	public static final String REGISTER_NODE = "register";
	
	private static KupSession mSession;
	private static List<KupRequest> mRequests = Collections.synchronizedList(new ArrayList<KupRequest>());
	
	private KupAPI()
	{
	}
	
	static KupAPIRequest createRequest(KupSession session, String api)
	{
		return new KupAPIRequest(String.format("%sapi/%s%s", 
				session.getAddress(),
				(Utils.isStringEmpty(session.getBucket()) ? "" : session.getBucket() + "/"),
				api));
	}
	
	static KupAPIRequest createNodeRequest(KupSession session, String api)
	{
		return new KupAPIRequest(String.format("%snodeapi/%s%s",
				session.getAddress(),
				(Utils.isStringEmpty(session.getBucket()) ? "" : session.getBucket() + "/"),
				api));
	}
	
	/**
	 * Store the globa session
	 * @param session
	 */
	public static void keepSession(KupSession session)
	{
		mSession = session;
	}
	
	/**
	 * Get current global logged-in session
	 * @return
	 */
	public static KupSession currentSession()
	{
		return mSession;
	}
	
	/**
	 * Send a blocking request, this will cancel previous request
	 * This method is usually used in thread
	 * @param request
	 * @return
	 */
	public static KupResponseResult send(KupRequest request)
	{
		cancel(request);
		mRequests.add(request);
		KupResponseResult result =  request.request(mSession);
		mRequests.remove(request);
		
		return result;
	}
	
	public static void cancel(KupRequest request) {
		if (mRequests.contains(request))
		{
			request.cancel();
			mRequests.remove(request);
		}
	}
	
	public static void cancelAll()
	{
		while (mRequests.size() > 0)
		{
			mRequests.remove(0);
		}
	}

	/**
	 * Send a asynchronous request, the request should have callback listener {@link IKupResponseListener} 
	 * @param request
	 */
	public static void sendAsync(KupRequest request)
	{
		request.requestAsync(mSession);
	}
}
