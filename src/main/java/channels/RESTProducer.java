package channels;

import java.util.HashMap;

import plugincore.PluginEngine;
import shared.MsgEvent;
import shared.MsgEventType;


public class RESTProducer implements Runnable {

	private boolean foundController = false;
    
    public RESTProducer() 
    {
       if(PluginEngine.replyRESTmap == null)
       {
    		PluginEngine.replyRESTmap = new HashMap<String,MsgEvent>();
       }		
    }
    
    public void run() {
        
    	try
    	{
    	//some init
    	}
    	catch(Exception ex)
    	{
    		System.err.println("LogProducer Initialization Failed:  Exiting");
    		System.err.println(ex);
    		return; //don't kill app if log can't be established
    		//System.exit(1);
    	}
    
    	PluginEngine.ProducerEnabled = true;
    	
    	MsgEvent le = new MsgEvent(MsgEventType.INFO,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,"LogProducer Started");
    	//MsgEventType msgType, String msgRegion, String msgAgent, String msgPlugin, String msgBody
    	PluginEngine.msgOutQueue.offer(le);
    	
    	while (PluginEngine.ProducerEnabled) {
        	try 
        	{    		
        		if(PluginEngine.ProducerActive)
        		{
        			synchronized(PluginEngine.msgOutQueue) 
            		{
            			while ((!PluginEngine.msgOutQueue.isEmpty())) 
            			{
            				MsgEvent me = PluginEngine.msgOutQueue.poll(); //get logevent
            				String restid = me.getParam("restid");
            				if(restid != null) //this is a return call put in return hash
            				{
            					PluginEngine.replyRESTmap.put(restid, me); 
            				}
            				else
            				{
            					//not sure what to do.. might be to controller output check
            					//System.out.println("Region=" + me.getMsgRegion() + " Agent=" + me.getMsgAgent() + " plugin=" + me.getMsgPlugin()  + " parmas=" + me.getParamsString());
            					//send to controller
            					me.setMsgRegion(PluginEngine.region);
            					me.setMsgAgent(PluginEngine.agentName);
            					me.setMsgPlugin("plugin/0");
            					PluginEngine.msgInQueue.offer(me);
            					/*
            					if(foundController)
            					{
            						
            					}
            				    */
            				}
            			}
            		}
        		}
        	
				Thread.sleep(100);
	        } 
        	catch (Exception ex) 
        	{
				System.out.println(ex);
			} 
        	
        }
    	System.out.println("LogProducer Disabled");   	
    	le = new MsgEvent(MsgEventType.INFO,PluginEngine.region,PluginEngine.agentName,PluginEngine.pluginSlot,"LogProducer Disabled");
    	PluginEngine.msgOutQueue.offer(le);
    	return;
    }
   
}