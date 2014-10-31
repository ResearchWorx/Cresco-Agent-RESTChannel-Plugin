package plugincore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import shared.MsgEvent;
import shared.MsgEventType;



public class WatchDog {
	private Timer timer;
	private long startTS;
	private Map<String,String> wdMap;
	
	public WatchDog() {
		  startTS = System.currentTimeMillis();
		  timer = new Timer();
	      timer.scheduleAtFixedRate(new WatchDogTask(), 500, PluginEngine.config.getWatchDogTimer());
	      wdMap = new HashMap<String,String>(); //for sending future WD messages
	      	  
	      //LogEvent le = new LogEvent("INFO",pluginSlot,"WatchDog timer set to " + config.getWatchDogTimer() + " milliseconds");
	        MsgEvent le = new MsgEvent(MsgEventType.INFO,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,"WatchDog timer set to " + PluginEngine.config.getWatchDogTimer() + " milliseconds");
	      
	      //logQueue.offer(new MsgEvent(MsgEventType.INFO,region,agentName,pluginSlot,msg));
		   
		  PluginEngine.msgOutQueue.offer(le);
	  }


	class WatchDogTask extends TimerTask {
	    public void run() {
	    	
	        //PluginEngine.this.getVersion(); 
	    	long runTime = System.currentTimeMillis() - startTS;
			 //LogEvent le = new LogEvent("WATCHDOG",pluginSlot,"Plugin Uptime " + String.valueOf(runTime) + "ms");
	    	 wdMap.put("runtime", String.valueOf(runTime));
	    	 wdMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
	    	 MsgEvent le = new MsgEvent(MsgEventType.WATCHDOG,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,wdMap);
		     
			 //PluginEngine.logQueue.offer(le);
			 PluginEngine.msgOutQueue.offer(le);
	      //timer.cancel(); //Not necessary because we call System.exit
	      //System.exit(0); //Stops the AWT thread (and everything else)
	    
	    }
	  }

}
