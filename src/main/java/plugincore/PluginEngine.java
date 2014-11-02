package plugincore;


import httpserv.httpServerEngine;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.configuration.SubnodeConfiguration;

import channels.RPCCall;
import shared.Clogger;
import shared.MsgEvent;
import shared.PluginImplementation;



public class PluginEngine {

	public static PluginConfig config;
	
	public static String pluginName;
	public static String pluginVersion;
	public static String plugin;
	public static String agent;
	public static String region;
	
	public static CommandExec commandExec;
	
	public static boolean RESTConsumerEnabled = false;
	public static boolean RESTConsumerActive = false;
	
	public static RPCCall rpcc;
	
	/*
	private static Thread RESTProducerThread;
	public static boolean RESTProducerActive = false;
	public static boolean RESTProducerEnabled = false;
	*/
	
	public static Map<String,MsgEvent> rpcMap;
	
	public static ConcurrentLinkedQueue<MsgEvent> logOutQueue;
	
	
	public static Clogger clog;

	public static ConcurrentLinkedQueue<MsgEvent> msgInQueue;
	
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
			   clog.error(msg);
			   version = "Unable to determine Version";
		   }
		   
		   return pluginName + "." + version;
	   }
	//steps to init the plugin
	public boolean initialize(ConcurrentLinkedQueue<MsgEvent> msgOutQueue,ConcurrentLinkedQueue<MsgEvent> msgInQueue, SubnodeConfiguration configObj, String region,String agent, String plugin)  
	{
		rpcMap = new ConcurrentHashMap<String,MsgEvent>();
		rpcc = new RPCCall();
		
		commandExec = new CommandExec();
		//this.msgOutQueue = msgOutQueue; //send directly to log queue
		this.msgInQueue = msgInQueue; //messages to agent should go here
		
		this.agent = agent;
		this.plugin = plugin;
		
		this.region = region;
		try{
			//no need for this plugin to use the log queue input
			/*
			if(msgOutQueue == null)
			{
				System.out.println("MsgOutQueue==null");
				return false;
			}
			*/
			logOutQueue = new ConcurrentLinkedQueue<MsgEvent>(); //create our own queue
			
			if(msgInQueue == null)
			{
				System.out.println("MsgInQueue==null");
				return false;
			}
			
			this.config = new PluginConfig(configObj);
			
			//create logger
			clog = new Clogger(msgInQueue,region,agent,plugin); //send logs directly to outqueue
			
			String startmsg = "Initializing Plugin: Region=" + region + " Agent=" + agent + " plugin=" + plugin + " version" + getVersion();
			clog.log(startmsg);
			
			
	    	System.out.println("Starting RESTChannel Plugin");
	    	try
	    	{
	    		System.out.println("Starting HTTP Service");
				httpServerEngine httpEngine = new httpServerEngine();
				Thread httpServerThread = new Thread(httpEngine);
		    	httpServerThread.start();		    
	    	}
	    	catch(Exception ex)
	    	{
	    		System.out.println("Unable to Start HTTP Service : " + ex.toString());
	    	}
	    	
	    	/*
	    	AMPQLogProducer v = new AMPQLogProducer();
	    	ProducerThread = new Thread(v);
	    	ProducerThread.start();
	    	while(!ProducerEnabled)
	    	{
	    		Thread.sleep(1000);
	    		String msg = "Waiting for AMPQProducer Initialization : Region=" + region + " Agent=" + agent + " plugin=" + plugin;
	    		clog.log(msg);
	    	}
	    	*/
	    	
	    	
	    	WatchDog wd = new WatchDog();
			
    		return true;
    		
		
		}
		catch(Exception ex)
		{
			String msg = "ERROR IN PLUGIN: : Region=" + region + " Agent=" + agent + " plugin=" + plugin + " " + ex.toString();
			clog.error(msg);
			return false;
		}
		
	}
	
	public void msgIn(MsgEvent me)
	{
		
		final MsgEvent ce = me;
		try
		{
		Thread thread = new Thread(){
		    public void run(){
		
		    	try 
		        {
					MsgEvent re = commandExec.cmdExec(ce);
					if(re != null)
					{
						re.setReturn(); //reverse to-from for return
						msgInQueue.offer(re); //send message back to queue
					}
					
				} 
		        catch(Exception ex)
		        {
		        	System.out.println("Controller : PluginEngine : msgIn Thread: " + ex.toString());
		        }
		    }
		  };
		  thread.start();
		}
		catch(Exception ex)
		{
			System.out.println("Controller : PluginEngine : msgIn Thread: " + ex.toString());        	
		}
		
	}
		
		
}
