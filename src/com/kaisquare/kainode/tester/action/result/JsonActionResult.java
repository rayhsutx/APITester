package com.kaisquare.kainode.tester.action.result;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.kaisquare.kainode.tester.VariableCollection;
import com.kaisquare.kainode.tester.action.TestActionStatus;

public class JsonActionResult extends ActionResult {
	
	private static final HashMap<String, Object> MAP_TYPE = new HashMap<String, Object>();

	public JsonActionResult(TestActionStatus status, VariableCollection variables, Object result) {
		super(status, variables, result);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void assignMappingValues(TestActionStatus status, Object result, Map<String, String> mapping) {
		
		if (mapping == null || mapping.size() == 0)
			return;
		
		Gson gson = new Gson();
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
					{
						List<Object> list = ((List<Object>)obj);
						int index = 0;
						if ("$last".equalsIgnoreCase(s))
							index = list.size() - 1;
						else
							index = Integer.parseInt(s);
						if (list.size() > index)
							obj = list.get(index);
						else
							obj = null;
					}
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
		if (obj == null)
			return null;
		
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
