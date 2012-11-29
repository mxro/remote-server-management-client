package com.appjangle.rsm.client;

/**
 * Configuration for the remote management server client.
 * 
 * @author <a href="http://www.mxro.de/">Max Rohde</a>
 * 
 */
public interface ClientConfiguration {

	/**
	 * The node to which commands will be posted.
	 * 
	 * @return
	 */
	public String getCommandsNode();

	public String getCommandsNodeSecret();

}
