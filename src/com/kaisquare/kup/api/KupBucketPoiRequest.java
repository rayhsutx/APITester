package com.kaisquare.kup.api;

import java.util.List;

/**
 * KUP POI request
 */
public class KupBucketPoiRequest extends KupUserRequest {

	public KupBucketPoiRequest() {
		
	}

	public KupBucketPoiRequest(IKupResponseListener listener) {
		super(listener);
	}

	@Override
	String getRequestAPIUrl() {
		return KupAPI.BUCKET_POIS;
	}

	@Override
	Class<? extends KupResponseResult> getDeserializedClass() {
		return KupBucketPoiResult.class;
	}

	public static class KupBucketPoiResult extends KupResponseResult
	{
		public List<KupPOI> pois;
	}
}
