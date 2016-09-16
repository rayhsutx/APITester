package com.kaisquare.gson;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.kaisquare.kainode.tester.action.ActionConfiguration;
import com.kaisquare.kainode.tester.action.Actions;
import com.kaisquare.kainode.tester.action.Actions.ActionNotFoundException;
import com.kaisquare.kainode.tester.action.IAction;
import com.kaisquare.kainode.tester.jobs.JobConfiguration.JobActionConfiguration;

public class JobActionConfigurationDeserializer implements JsonDeserializer<JobActionConfiguration> {

	@Override
	public JobActionConfiguration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Gson gson = new Gson();
		Class<JobActionConfiguration> cls = JobActionConfiguration.class;
		JobActionConfiguration obj = gson.fromJson(json, JobActionConfiguration.class);
		
		try {
			JsonElement configJson = json.getAsJsonObject().get("config");
			IAction act = Actions.create(obj.getType());
			
			if (!act.getConfigurationClass().equals(ActionConfiguration.class))
			{
				ActionConfiguration actionConfig = gson.fromJson(configJson, act.getConfigurationClass());
				Field field = cls.getDeclaredField("config");
				field.setAccessible(true);
				field.set(obj, actionConfig);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			new JsonParseException(e);
		} catch (ActionNotFoundException e) {
		}
		
		return obj;
	}

}
