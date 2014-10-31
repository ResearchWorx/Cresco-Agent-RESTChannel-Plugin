package channels;


import java.util.HashMap;

import plugincore.PluginEngine;
import shared.MsgEvent;
import shared.MsgEventType;
import shared.RandomString;


public class RESTConsumer {

		private static RandomString rands; 
		public RESTConsumer() 
		{
			rands = new RandomString(15);
			PluginEngine.replyRESTmap = new HashMap<String,MsgEvent>();
			PluginEngine.ConsumerEnabled = true;
	    }

	
	public String call(MsgEvent me)
	{
		String restid = null;
		if(PluginEngine.ConsumerActive)
		{
			try
			{
				restid = rands.nextString();
				me.setParam("restid",restid);
				PluginEngine.msgInQueue.add(me);
				return restid;
			}
			catch(Exception ex)
			{
				System.out.println(ex);
				//LogEvent le = new LogEvent("ERROR",AgentEngine.config.getAgentName(),"Controller ERROR: " + ex.toString());
				MsgEvent le = new MsgEvent(MsgEventType.ERROR,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,"REST Consumer ERROR: " + ex.toString());
				//MsgEvent le = new MsgEvent(MsgEventType.INFO,AgentEngine.config.getRegion(),AgentEngine.config.getAgentName(),null,"Controller Started");
				PluginEngine.msgOutQueue.offer(le);
				return restid;
			}
		}
		return restid;
		
	}
}
