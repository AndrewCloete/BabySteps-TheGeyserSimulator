/* --------------------------------------------------------------------------------------------------------
 * DATE:	12 Apr 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Abstracts TCP socket communications to message queues. Server side implementation.
 * 			
 * 			Runs a TCP listerner thread
 * 				- pushes received strings to a given inbound_queue LinkedList<String>
 * 				- waits for content on outbound_queue, and then writes it out.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: Provides a TCP interface to the Simulator Message queues
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserm2m.simulator;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class TCPServer implements Runnable {

	private LinkedList<String> inbound_queue; 
	private LinkedList<String> outbound_queue;
	private int port;
	private ServerSocket listener;
	
	public TCPServer(int port, LinkedList<String> inbound_queue, LinkedList<String> outbound_queue){
		this.inbound_queue = inbound_queue;
		this.outbound_queue = outbound_queue;
		this.port = port;
		
		try {
			this.listener = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to construct ServerSocket.");
		}
	}
	
	@Override
	public void run() {
		try {
			
			/* Server loop: forever
			 	* Wait for client to connect to port. (Blocking)
			 	* Get input and output stream references
			 	* 
			 	* Conversation loop: while client is connected
			 		* Read a line from client
			 		* Check if client has disconnected (line == null);
			 		* Push line on inbound_queue
			 		* Wait for content on outbound_queue
			 		* Pop content off outbound_queue
			 		* Write content to client
			 * 
			 */
			
			while(true){	//To-do: add thread flag to ensure graceful shutdown
				Socket client = listener.accept(); 
				
				InputStream in = client.getInputStream();
				OutputStream out = client.getOutputStream();
				
				while(client.isConnected()){	
					BufferedReader bin = new BufferedReader(new InputStreamReader(in));
					String buffer = bin.readLine();
					
					if(buffer == null) 	//Check of client disconnected.
						break;	 		//Break out of conversation loop
					
					inbound_queue.push(buffer);
					
					while(outbound_queue.isEmpty()){ //Wait for controller to respond;
						try {
							Thread.sleep(1);	//Is there a more graceful way to listen?
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					PrintWriter pout = new PrintWriter(out, true);
					pout.println(outbound_queue.pop());
				}	
				
				System.out.println("Client disconnected");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error in TCP converstation loop.");
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.listener.close();
	}

}

/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */