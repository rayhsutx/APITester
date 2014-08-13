package com.kaisquare.kup.api;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.annotations.SerializedName;
import com.kaisquare.utils.AppLogger;

/**
 * KUP Request
 */
public abstract class KupRequest implements Runnable, IKupRawDataReceivedListener {
	
	private String mApi;
	private String mApiUrl;
	private KupSession mSession;
	private Class<? extends KupResponseResult> mClass;
	private volatile boolean mCancel = false;
	private KupAPIRequest mRequest;
	private IKupRawDataReceivedListener mDataListener;
	
	private ExecutorService mThreadPool;
	private IKupResponseListener mListener;
	private boolean mIsNodeRequest = false;
	
	/**
	 * Default constructor
	 */
	public KupRequest() {}
	
	/**
	 * Create the request with {@Link IKupResponseListener}
	 * @param listener specific listenr to get result from the request
	 */
	public KupRequest(IKupResponseListener listener)
	{
		setResponseListener(listener);
	}
	
	public void setResponseListener(IKupResponseListener listener)
	{
		mListener = listener;
	}
	
	public void setRawDataReceivedListener(IKupRawDataReceivedListener listener)
	{
		mDataListener = listener;
	}
	
	public void setNodeRequest(boolean isNode)
	{
		mIsNodeRequest = isNode;
	}
	
	/**
	 * Cancel current request
	 */
	public void cancel()
	{
		mCancel = true;
		if (mRequest != null)
			mRequest.cancel();
		
		if (mThreadPool != null && !mThreadPool.isShutdown())
			mThreadPool.shutdownNow();
	}
	
	/**
	 * Send this request with session, some request needs user session to retrieve data
	 * if the request doesn't need user session, then just set it null
	 * <br /><br />
	 * <b>NOTE: this method is not asynchronous, it will be block until the request is done</b>
	 * @param session user session, or null if it's not necessary
	 * @return response from server
	 */
	public KupResponseResult request(KupSession session)
	{
		return request(session, getRequestAPIUrl(), getDeserializedClass());
	}
	
	protected KupResponseResult request(KupSession session, String apiUrl, Class<? extends KupResponseResult> classOfReturn)
	{
		if (session == null)
			return new KupFailureResponseResult(-1, "Session is null");
		
		mCancel = false;
		mSession = session;
		mRequest = mIsNodeRequest ? KupAPI.createNodeRequest(session, apiUrl) : KupAPI.createRequest(session, apiUrl);
		if (mDataListener != null)
			mRequest.setRawDataListener(this);
		
		KupAPIResponse<? extends KupResponseResult> response = null;
		setPostData();
		response = mRequest.request(getParameter(), getInputStream(), classOfReturn, session.getCookie());
		
		KupResponseResult result = null;
		if (response != null && response.getStatus() == 200)
		{
			result = response.getData();
			if (result != null)
			{
				session.setCookie(response.getCookie());
				result.setSession(session);
				AppLogger.d(this, "api: " + apiUrl + ", result = " + result.result + ", reason = " + result.reason);
			}
		}
		
		if (result == null && response != null)
			result = new KupFailureResponseResult(response.getStatus(), response.getError());
		
		return result;
	}
	
	/**
	 * As same as {@link request}, but this function is non-blocking method
	 * please use {@link KupRequest(IKupResponseListener)} to add the callback listener to get the result
	 * @param session
	 */
	public void requestAsync(KupSession session)
	{
		requestAsync(getRequestAPIUrl(), getRequestAPIUrl(), session, getDeserializedClass());
	}
	
	protected void requestAsync(String api, String apiUrl, KupSession session, Class<? extends KupResponseResult> classResult)
	{
		mApi = api;
		mApiUrl = apiUrl;
		mSession = session;
		mClass = classResult;
		
		cancel();		
		
		mThreadPool = Executors.newSingleThreadExecutor();
		mThreadPool.execute(this);
	}
	
	@Override
	public void run()
	{
		KupResponseResult result = request(mSession, mApiUrl, mClass);
		onResult(mApi, result);
	}
	
	/**
	 * Get this request's session
	 * @return session
	 */
	public KupSession getSession()
	{
		return mSession;
	}
	
	@Override
	public boolean onKupRawDataRead(KupRequest request, byte[] data, int size) {
		if (mDataListener != null)
			return mDataListener.onKupRawDataRead(this, data, size);
		
		return false;
	}
	
	protected void onResult(String api, KupResponseResult result)
	{
		if (mListener != null)
			mListener.onKupResponse(api, result);
	}

	/**
	 * Get POST parameters, this refers to subclass serializable fields
	 * if the fields which not be serialized, add symbol 'transient'
	 * or set the annotation with '@SerializeName' to specify the serialized name
	 * @return
	 */
	protected HashMap<String, String> getParameter()
	{
		HashMap<String, String> param = new HashMap<String, String>();
		Class<?>[] allcls = { getClass().getSuperclass(), getClass() };
		
		for (Class<?> cls : allcls)
		{
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields)
			{
				if (field.getModifiers() == Modifier.TRANSIENT)
					continue;
				
				try {
					field.setAccessible(true);
					String value = "";
					Object object = field.get(this);
					if (object instanceof Integer)
						value = Integer.toString((Integer)object);
					else if (object instanceof String)
						value = (String)object;
						
					Annotation[] annotations = field.getAnnotations();
					for (Annotation annotation : annotations)
					{
						if (annotation instanceof SerializedName)
						{
							param.put(((SerializedName)annotation).value(), value);
							break;
						}
					}
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
		
		return param;
	}
	
	/**
	 * Get POST {@link InputStream}, maybe a file or binary data that needs to be uploaded
	 * @return {@link InputStream}, if there's no stream, just return null
	 */
	protected InputStreamWrapper getInputStream()
	{
		return null;
	}
	
	/**
	 * Override the POST data which will be sent to server
	 */
	protected abstract void setPostData();
	/**
	 * API request REST path {@link KupAPI}
	 * @return
	 */
	abstract String getRequestAPIUrl();
	/**
	 * The class type for the result which will be converted from JSON
	 * @return
	 */
	abstract Class<? extends KupResponseResult> getDeserializedClass();
}
