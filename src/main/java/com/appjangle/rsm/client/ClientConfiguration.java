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

	/**
	 * Secret for the node to which commands will be posted.
	 * 
	 * @return
	 */
	public String getCommandsNodeSecret();

	/**
	 * Node to which responses from the server shall be posted.
	 * 
	 * @return
	 */
	public String getResponsesNode();

	/**
	 * Secret for the node to which responses from the server shall be posted.
	 * 
	 * @return
	 */
	public String getResponseNodeSecret();

}
