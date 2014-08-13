package com.kaisquare.kainode.tester.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

public class JsonActionResult extends ActionResult {
	
	private static final HashMap<String, Object> MAP_TYPE = new HashMap<String, Object>();

	public JsonActionResult(TestActionStatus status, Object result) {
		super(status, result);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void assignMappingValues(Map<String, String> createMappingObject,
			TestActionStatus status, Object result, Map<String, String> mapping) {
		
		if (mapping == null || mapping.size() == 0)
			return;
		
		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		HashMap<String, Object> jsonMap = gson.fromJson((String)result, MAP_TYPE.getClass());
		
		Iterator<Entry<String, String>> iterator = mapping.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String valueOfPath = entry.getValue();
			String value = "";
			
			if (valueOfPath.contains(":"))
			{
				String[] paths = valueOfPath.split("\\:");
				Object obj = jsonMap.get(paths[0]);
				int i = 1;
				while (i < paths.length && obj != null)
				{
					String s = paths[i];
					if (obj instanceof Map)
						obj = ((Map<String, Object>)obj).get(s);
					else if (obj instanceof List)
						obj = ((List<Object>)obj).get(Integer.parseInt(paths[i]));
					else
						break;
					
					i++;
				}				
				value = formatValue(obj);
			}
			else
				value = formatValue(jsonMap.get(valueOfPath));
			
			putVariable(key, value);
		}
	}

	private String formatValue(Object obj) {
		String value;
		if (obj instanceof Double)
		{
			int n = ((Double)obj).intValue();
			double d = (double) obj;
			if (d - n == 0)
				value = String.valueOf(n);
			else
				value = String.valueOf(obj);
		}
		else
			value = String.valueOf(obj);
		
		return value;
	}

}
