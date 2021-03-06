package com.kaisquare.kainode.tester.action.kaisync;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;

import com.kaisquare.kainode.tester.action.ActionConfiguration;
import com.kaisquare.kainode.tester.action.Actions;
import com.kaisquare.kainode.tester.action.RequestAction;
import com.kaisquare.kainode.tester.action.TestActionStatus;
import com.kaisquare.kainode.tester.action.result.ActionResult;
import com.kaisquare.kainode.tester.action.result.EmptyActionResult;
import com.kaisquare.kaisync.ISyncWriteFile;
import com.kaisquare.kaisync.KAISync;
import com.kaisquare.kaisync.platform.Command;
import com.kaisquare.kaisync.platform.CommandClient;
import com.kaisquare.kaisync.platform.ICommandReceivedListener;
import com.kaisquare.kaisync.platform.IPlatformSync;
import com.kaisquare.kaisync.platform.MessagePacket;
import com.kaisquare.kaisync.transport.PThreadFactory;
import com.kaisquare.kaisync.utils.AppLogger;
import com.kaisquare.kaisync.utils.Utils;

public class KAISyncAction extends RequestAction implements ICommandReceivedListener {
	
	private ConcurrentHashMap<String, String> sentCommands;
	private IPlatformSync platformSync = null;

	@Override
	public String getActionName() {
		return "KAISync";
	}

	@Override
	public String getActionType() {
		return Actions.ACTION_KAISYNC;
	}
	
	@Override
	public Class<? extends ActionConfiguration> getConfigurationClass()
	{
		return KAISyncActionConfiguration.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ActionResult submit(ActionConfiguration c) {
		KAISyncActionConfiguration config = (KAISyncActionConfiguration) c;
		TestActionStatus resultStatus = TestActionStatus.Ok;
		String host = getVariable("sync-host");
		int port = Integer.parseInt(getVariable("sync-port"));
		String keystore = getVariable("keystore");
		String keypass = getVariable("keypass");
		String nodeId = getVariable("cloud-platform-device-id");
		CommandClient cmdClient = null;
		
		String syncCommand = parseVariable(config.getSyncCommand());
		String syncMacAddress = parseVariable(config.getSyncMacAddress());
		String syncEvent = parseVariable(config.getSyncEvent());
		String syncEventVideo = parseVariable(config.getSyncEventVideo());
		
		if (Utils.isStringEmpty(host))
			throw new NullPointerException("empty 'sync-host'");
		
		try {
			platformSync = KAISync.newPlatformClient(host, port, keystore, keypass);
		} catch (IOException e) {
			AppLogger.e(this, e, "");
			return new EmptyActionResult(TestActionStatus.Error, getVariables());
		}
		try {
			if (config.isBindCommand())
				cmdClient = bindCommand(platformSync, nodeId, syncMacAddress, config.getTimeout());
			
			if (!Utils.isStringEmpty(syncCommand))
			{
				if (Utils.isStringEmpty(nodeId))
					throw new NullPointerException("empty 'cloud-platform-device-id'");
				
				sentCommands = new ConcurrentHashMap<>(config.getCount());
				if (config.isWaitCommand() && cmdClient == null)
					cmdClient = bindCommand(platformSync, nodeId, syncMacAddress, config.getTimeout());
				
				List<String> parameters = (List<String>) config.getData().get("parameters");
				for (int i = 0; i < config.getCount(); i++)
				{
					Command command = new Command(UUID.randomUUID().toString(), syncCommand, "");
					if (parameters != null && !parameters.isEmpty())
					{
						for (String p : parameters)
							command.getParameters().add(parseVariable(p));
					}
					List<Command> list = Arrays.asList(command);

					sentCommands.put(command.getId(), command.getCommand());
					AppLogger.i(this, "send command (%d:%s): %s", (i + 1), syncCommand, platformSync.sendCommands(nodeId, syncMacAddress, list));					
				}
			}
			if (!Utils.isStringEmpty(syncEvent))
			{
				String eventData = parseVariable(config.getData().get("data") != null ? config.getData().get("data").toString() : "");
				String eventType = parseVariable(config.getData().get("type") != null ? config.getData().get("type").toString() : "");
				String eventTime = parseVariable(config.getData().get("time") != null ? config.getData().get("time").toString() : "");
				String eventDeviceId = parseVariable(config.getData().get("deviceid") != null ? config.getData().get("deviceid").toString() : "");
				String eventChannelId = parseVariable(config.getData().get("channelid") != null ? config.getData().get("channelid").toString() : "");
				String binary = parseVariable(config.getData().get("binary") != null ? config.getData().get("binary").toString() : "");
				String eventVideo = parseVariable(config.getData().get("video") != null ? config.getData().get("video").toString() : "");
				byte[] eventBinary = !Utils.isStringEmpty(binary) ? Base64.decodeBase64(binary) : null;
				
				ExecutorService threadPool = Executors.newFixedThreadPool(config.getThreads() > 0 ? config.getThreads() : 1, new PThreadFactory("event-video"));
				LinkedList<Future> futures = new LinkedList<Future>();
				try {
			        for (int i = 0; i < config.getCount(); i++)
			        {
			        	String eventId = UUID.randomUUID().toString();
			        	MessagePacket packet = new MessagePacket();
			        	packet.put("id", eventId);
				        packet.put("data", eventData);
				        packet.put("type", eventType);
				        packet.put("time", eventTime);
				        packet.put("deviceid", eventDeviceId);
				        packet.put("channelid", eventChannelId);
				        
				        if (eventBinary != null)
				        	packet.putBytes("binary", eventBinary);
				        
			        	AppLogger.i(this, "push event (%d): %s (id=%s)",
			        			(i + 1), platformSync.pushEvent(packet.toBytes()), eventId);
			        	if (!Utils.isStringEmpty(eventVideo))
			        	{
			        		File file = new File(eventVideo);
			        		if (file.exists())
			        		{
			        			futures.add(threadPool.submit(
				        			new EventVideoFileUpload(
				        					platformSync, 
				        					file, 
				        					getVariable("file-keystore"), 
				        					getVariable("file-keypass"),
				        					eventId,
				        					eventDeviceId,
				        					eventChannelId,
				        					eventData,
				        					eventTime)));
			        		}
			        		else
			        			AppLogger.e(this, "file '%s' not exists", eventVideo);
			        	}
			        }
				} finally {
					if (futures.size() > 0)
					{
						AppLogger.i(this, "wait for event video uploads");
						for (Future f : futures)
						{
							try {
								AppLogger.i(this, "event video %s done", f.get());
							} catch (InterruptedException | ExecutionException e) {}
						}
					}
					threadPool.shutdown();
				}
			}
		} finally {
			if (cmdClient != null)
			{
				if (config.isWaitCommand())
				{
					long start = System.currentTimeMillis();
					AppLogger.i(this, "waiting for command response (remaining %d)", sentCommands.size());
					while (sentCommands.size() > 0)
					{
						try {
							Thread.sleep(config.getCommandTimeout() > 1000 ? 1000 : config.getCommandTimeout());
						} catch (InterruptedException e) {
							break;
						}
						if (sentCommands.size() > 0 && System.currentTimeMillis() - start >= config.getCommandTimeout())
						{
							AppLogger.w(this, "wait for commands timeout (%s-%s)", nodeId, syncMacAddress);
							Entry[] items = sentCommands.entrySet().toArray(new Entry[0]);
							AppLogger.i(this, "----no response commands----");
							for (Entry item : items)
							{
								AppLogger.i(this, "%s > %s", item.getKey(), item.getValue());
							}
							AppLogger.i(this, "----------------------------");
							resultStatus = TestActionStatus.Failed;
							break;
						}
					}
				}
				if (config.isBindCommand())
				{
					try {
						Thread.sleep(config.getTimeout());
					} catch (InterruptedException e) {
					}
				}
				cmdClient.close();
			}
			platformSync.close();
		}
		EmptyActionResult result = new EmptyActionResult(resultStatus, getVariables());
		
		return result;
	}

	private CommandClient bindCommand(IPlatformSync platformSync, String nodeId, String syncMacAddress, int timeout) {
		CommandClient cmdClient = null;
		try {
			cmdClient = platformSync.bindCommands(nodeId, syncMacAddress, this);
			cmdClient.start();
			cmdClient.awaitReady(timeout);
		} catch (Exception e) {
			if (cmdClient != null) cmdClient.close();
			throw new RuntimeException("unable to bind command " + nodeId + "-" + syncMacAddress);
		}
		
		return cmdClient;
	}

	@Override
	public boolean onCommandReceived(String identifier, String macAddress, List<Command> commands) {
		for (Command cmd : commands)
		{
			try {
				if (!Utils.isStringEmpty(cmd.getOriginalId()))
				{
					AppLogger.i(this, "recv %s: %s=%s",
							cmd.getOriginalId(),
							cmd.getCommand(),
							cmd.getParameters().get(0));
					
					sentCommands.remove(cmd.getOriginalId());
				}
				else
				{
					AppLogger.i(this, "received cloud command %s", cmd.getCommand());
					Command response = new Command(UUID.randomUUID().toString(), cmd.getCommand(), cmd.getOriginalId());
					response.getParameters().add("Success");
					AppLogger.i(this, "reply '%s': %s",
							cmd.getCommand(),
							platformSync.sendCommands(identifier, macAddress, Arrays.asList(response)));
				}
			} catch (Exception e) {
				AppLogger.e(this, e, "");
			}
		}
		
		return true;
	}
	
	static class EventVideoFileUpload implements Callable<String>
	{
		private File file;
		private String keystore;
		private String keypass;
		private IPlatformSync platformSync;
		private String eventId;
		private String eventDeviceId;
		private String eventChannelId;
		private String eventData;
		private String eventTime;
		
		public EventVideoFileUpload(IPlatformSync platformSync, File videoFile, String keystore, String keypass,
				String eventId, String eventDeviceId, String eventChannelId, String eventData, String eventTime)
		{
			this.platformSync = platformSync;
			this.file = videoFile;
			this.keystore = keystore;
			this.keypass = keypass;
			
			this.eventId = eventId;
			this.eventDeviceId = eventDeviceId;
			this.eventChannelId = eventChannelId;
			this.eventData = eventData;
			this.eventTime = eventTime;
		}

		@Override
		public String call() {
			String extname = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			String filename = String.format("%s_%s.%s", eventDeviceId, eventId, extname);
			ISyncWriteFile syncFile = platformSync.syncEventVideoFile(eventId, eventDeviceId, filename);
			syncFile.setKeystore(keystore, keypass);
			
			if (syncFile != null && syncFile.getID() != null)
			{
				AppLogger.i(this, "sending video file %s", file.getName());
				InputStream in = null;
				OutputStream out = null;
				
				try {
					in = new BufferedInputStream(new FileInputStream(file));
					out = syncFile.getOutputStream();
					
					byte[] buf = new byte[8192];
					int read = 0;
					
					while ((read = in.read(buf)) > 0)
						out.write(buf, 0, read);
					
					out.flush();
					
					MessagePacket packet = new MessagePacket();
		        	packet.put("id", eventId);
		        	packet.put("data", eventData);
			        packet.put("type", "event-recording");
			        packet.put("time", eventTime);
			        packet.put("deviceid", eventDeviceId);
			        packet.put("channelid", eventChannelId);
			        AppLogger.d(this, "notify event video event: %s (%s)", 
			        		platformSync.pushEvent(packet.toBytes()), filename);
					
				} catch (IOException e) {
					AppLogger.e(this, e, "");
				} finally {
					if (in != null)
					{
						try {
							in.close();
						} catch (IOException e) {}
					}
					
					if (out != null)
					{
						try {
							out.close();
						} catch (IOException e) {}
					}
				}
				
				return filename;
			}
			else
				AppLogger.e(this, "unable to open remote file for %s", filename);
			
			return "";
		}
		
	}
}
