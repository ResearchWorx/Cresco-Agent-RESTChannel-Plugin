package plugincore;


import httpserv.httpServerEngine;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.configuration.SubnodeConfiguration;

import channels.RESTConsumer;
import channels.RESTProducer;
import shared.MsgEvent;
import shared.MsgEventType;
import shared.PluginImplementation;



public class PluginEngine {

	public static PluginConfig config;
	
	public static String pluginName;
	public static String pluginSlot;
	public static String agentName;
	public static String region;
	
	public static HashMap<String,MsgEvent> replyRESTmap;
	public static ConcurrentLinkedQueue<MsgEvent> msgInQueue;
	public static RESTConsumer restConsumer;
	public static boolean ConsumerActive = false;
	public static boolean ConsumerEnabled = false;
	
	private static Thread ProducerThread;
	public static ConcurrentLinkedQueue<MsgEvent> msgOutQueue;
	public static boolean ProducerActive = false;
	public static boolean ProducerEnabled = false;
	
	//public static boolean watchDogActive = false; //agent watchdog on/off
	
	
	public PluginEngine()
	{
		pluginName="RESTPlugin";
	}
	public void shutdown()
	{
		System.out.println("Implement Shutdown in Plugin");
	}
	public String getName()
	{
		   return pluginName; 
	}
	public String getVersion() //This should pull the version information from jar Meta data
    {
		   String version;
		   try{
		   String jarFile = PluginImplementation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		   File file = new File(jarFile.substring(5, (jarFile.length() -2)));
           FileInputStream fis = new FileInputStream(file);
           @SuppressWarnings("resource")
		   JarInputStream jarStream = new JarInputStream(fis);
		   Manifest mf = jarStream.getManifest();
		   
		   Attributes mainAttribs = mf.getMainAttributes();
           version = mainAttribs.getValue("Implementation-Version");
		   }
		   catch(Exception ex)
		   {
			   String msg = "Unable to determine Plugin Version " + ex.toString();
			   System.err.println(msg);
			   //logQueueOutgoing.offer(new LogEvent("ERROR",pluginSlot,msg));
			   msgOutQueue.offer(new MsgEvent(MsgEventType.INFO,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,msg));
			   version = "Unable to determine Version";
		   }
		   
		   return pluginName + "." + version;
	   }
	//steps to init the plugin
	public boolean initialize(ConcurrentLinkedQueue<MsgEvent> msgOutQueue,ConcurrentLinkedQueue<MsgEvent> msgInQueue, SubnodeConfiguration configObj, String region,String agentName, String plugin)  
	{
		this.msgOutQueue = msgOutQueue;
		this.msgInQueue = msgInQueue;
		
		this.agentName = agentName;
		this.pluginSlot = pluginSlot;
		this.region = region;
		try{
			this.config = new PluginConfig(configObj);
			
			String startmsg = "Initializing Plugin: " + getVersion();
			System.err.println(startmsg);
			
			if(msgOutQueue == null)
			{
				System.out.println("MsgOutQueue==null");
			}
			if(msgInQueue == null)
			{
				System.out.println("MsgInQueue==null");
			}
			
			//logQueueOutgoing.offer(new LogEvent("INFO",pluginID,startmsg));
			msgOutQueue.offer(new MsgEvent(MsgEventType.INFO,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,startmsg));
			
	    	System.out.println("Starting AMPQChannel Plugin");
	    
	    	try
	    	{
	    		System.out.println("REST HTTP Service");
				httpServerEngine httpEngine = new httpServerEngine();
				Thread httpServerThread = new Thread(httpEngine);
		    	httpServerThread.start();		    
	    	}
	    	catch(Exception ex)
	    	{
	    		System.out.println("REST Plugin Init error: " + ex.toString());
	    		return false;
	    	}
    		
			//Create Incoming log Queue wait to start
	    	restConsumer = new RESTConsumer();
    		while(!ConsumerEnabled)
	    	{
	    		Thread.sleep(1000);
	    		String msg = "Waiting for RESTConsumer Initialization...";
	    		msgOutQueue.offer(new MsgEvent(MsgEventType.INFO,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,msg));
				System.out.println(msg);
	    	}
	    	
	    	
	    	RESTProducer v = new RESTProducer();
	    	ProducerThread = new Thread(v);
	    	ProducerThread.start();
	    	while(!ProducerEnabled)
	    	{
	    		Thread.sleep(1000);
	    		String msg = "Waiting for AMPQProducer Initialization...";
	    		msgOutQueue.offer(new MsgEvent(MsgEventType.INFO,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,msg));
				System.out.println(msg);
	    	}
	    	
	    	PluginEngine.ConsumerActive = true;
	    	PluginEngine.ProducerActive = true;
	    	
	    	
	    	WatchDog wd = new WatchDog();
			
    		return true;
    		
		
		}
		catch(Exception ex)
		{
			String msg = "ERROR IN PLUGIN: " + ex.toString();
			System.err.println(msg);
			//logQueue.offer(new LogEvent("ERROR",pluginID,msg));
			msgOutQueue.offer(new MsgEvent(MsgEventType.INFO,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,msg));
			
			return false;
		}
		
	}
	
	public MsgEvent msgIn(MsgEvent ce)
	{
		if(ce.getMsgType() == MsgEventType.DISCOVER)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("help\n");
			sb.append("show\n");
			sb.append("show_name\n");
			sb.append("show_version\n");
			ce.setMsgBody(sb.toString());
		}
		else if(ce.getMsgType() == MsgEventType.EXEC) //Execute and respond to execute commands
		{
			if(ce.getParam("cmd").equals("show") || ce.getParam("cmd").equals("?") || ce.getParam("cmd").equals("help"))
			{
			StringBuilder sb = new StringBuilder();
			sb.append("\nPlugin " + getName() + " Help\n");
			sb.append("-\n");
			sb.append("show\t\t\t\t\t Shows Commands\n");
			sb.append("show name\t\t\t\t Shows Plugin Name\n");
			sb.append("show version\t\t\t\t Shows Plugin Version");
			ce.setMsgBody(sb.toString());
			}
			else if(ce.getParam("cmd").equals("show_version"))
			{
				ce.setMsgBody(getVersion());
			}
			else if(ce.getParam("cmd").equals("show_name"))
			{
				ce.setMsgBody(getName());
			}
		}
		else
		{
			ce.setMsgBody("Plugin Command [" + ce.getMsgType().toString() + "] unknown");
		}
		return ce;
	}
		
}
