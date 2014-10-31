package shared;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.configuration.SubnodeConfiguration;

public interface PluginInterface {

	   public boolean initialize(ConcurrentLinkedQueue<MsgEvent> msgOutQueue,ConcurrentLinkedQueue<MsgEvent> msgInQueue, SubnodeConfiguration configObj, String region,String agentName, String plugin);
	   public MsgEvent msgIn(MsgEvent command);
	   public String getName();
	   public String getVersion();
	   public void shutdown();
	}


