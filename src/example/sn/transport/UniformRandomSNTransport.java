package example.sn.transport;

import example.sn.node.SNNode;
import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class UniformRandomSNTransport implements Transport {
	//---------------------------------------------------------------------
	//Parameters
	//---------------------------------------------------------------------

	/** 
	 * String name of the parameter used to configure the minimum latency.
	 * @config
	 */	
	private static final String PAR_MINDELAY = "mindelay";	
		
	/** 
	 * String name of the parameter used to configure the maximum latency.
	 * Defaults to {@value #PAR_MINDELAY}, which results in a constant delay.
	 * @config 
	 */	
	private static final String PAR_MAXDELAY = "maxdelay";	
		
	//---------------------------------------------------------------------
	//Fields
	//---------------------------------------------------------------------

	/** Minimum delay for message sending */
	private final long min;
		
	/** Difference between the max and min delay plus one. That is, max delay is
	* min+range-1.
	*/
	private final long range;

		
	//---------------------------------------------------------------------
	//Initialization
	//---------------------------------------------------------------------

	/**
	 * Reads configuration parameter.
	 */
	public UniformRandomSNTransport(String prefix)
	{
		min = Configuration.getLong(prefix + "." + PAR_MINDELAY);
		long max = Configuration.getLong(prefix + "." + PAR_MAXDELAY,min);
		if (max < min) 
		   throw new IllegalParameterException(prefix+"."+PAR_MAXDELAY, 
		   "The maximum latency cannot be smaller than the minimum latency");
		range = max-min+1;
	}

	//---------------------------------------------------------------------

	/**
	* Returns <code>this</code>. This way only one instance exists in the system
	* that is linked from all the nodes. This is because this protocol has no
	* node specific state.
	*/
	public Object clone()
	{
		return this;
	}

	//---------------------------------------------------------------------
	//Methods
	//---------------------------------------------------------------------

	/**
	 * Delivers the message with a random
	 * delay, that is drawn from the configured interval according to the uniform
	 * distribution.
	*/
	public void send(Node src, Node dest, Object msg, int pid)
	{
		// avoid calling nextLong if possible
		long delay = (range==1?min:min + CommonState.r.nextLong(range));
		if (((SNNode)dest).isOnline())
			EDSimulator.add(delay, msg, dest, pid);
	}

	/**
	 * Returns a random
	 * delay, that is drawn from the configured interval according to the uniform
	 * distribution.
	*/
	public long getLatency(Node src, Node dest)
	{
		return (range==1?min:min + CommonState.r.nextLong(range));
	}


}
