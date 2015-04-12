/* --------------------------------------------------------------------------------------------------------
 * DATE:	10 Apr 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: This software simulates geyser instrumentation firmware.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: - A stand-alone application that acts and behaves in every way like geyser firmware, running on 
 * 				instrumentation attached to a real geyser. 
 * 			- gain insight in firmware design requirements
 * 			- use as a tool to benchmark the M2M network system
 * 			- gain insight in geyser behavior (model assessment)
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserm2m.simulator;

import java.util.LinkedList;

public class GeyserSimulator {
	
	//API Read-only parameters
	private char critical_flags = 0;

	//API Read-Write parameters
	private enum Control_mode {CLOSED, OPEN}
	private static Control_mode control_mode;
	private static boolean element_request = false;
	
	
	//Internal parameters
	private static final float default_setpointHigh = 55;
	private static final float default_setpointLow = 45;
	
	private static LinkedList<String> commandQueue = new LinkedList<String>();
	private static LinkedList<String> replyQueue = new LinkedList<String>();
	
	private static int PORT = 3000;
	
	/*
	 * Represents the main entry point of firmware.
	 */
	public static void main(String [] args)
	{
		System.out.println("Project: Baby steps.\n");
		
		/*
		 * Initialize
			 * Thread Listeners that checks bursts/power failures "Interrupt routine"
			 * Register message queues (Command-queue and Reply-Queue) with appropriate user facing handler(s)
			 * Initialize VirtualGeyser;
			 	* Wait for geyserReady
			 	* start VirtualGeyserThread
			 * Set Control_mode to default 
		 */
		System.out.println("Starting TCP server...");
		TCPServer tcp_server = new TCPServer(PORT, commandQueue, replyQueue);
		Thread tcp_server_thread = new Thread(tcp_server);
		tcp_server_thread.start();
		System.out.println("TCP server started.\n");
		
		System.out.println("Starting Virtual Geyser...");
		VirtualGeyser v_geyser = new VirtualGeyser();
		while(!v_geyser.geyserReady());
		Thread virtualgeyser_thread = new Thread(v_geyser);
		virtualgeyser_thread.start();
		System.out.println("Virtual Geyser started.\n");
		
		
		/* MAIN PROGRAM LOOP:
			 * Control routine:	Lowest level hardware controller
			 * API Command service: Process commands from API
			 * 
			 * sleep for about a second.
			 * LOOP forever
		 */
		
		//Hard coded for testing
		control_mode = Control_mode.CLOSED;
		
		while(true){

			//Debug messages
			System.out.println("Control mode: " + Control_mode.CLOSED);
			System.out.println("T_internal: " + v_geyser.getInternalTemp());
			System.out.println("Element state: " + v_geyser.getElementState());
			System.out.println();
			
			/* -----------------------------------------------------------------------------*/
			
			/*
			 * Control routine: 
				 * CLOSED:
				 * 		- Switch element according to internal temperature, between default dead-bands.
				 * OPEN:
				 * 		- Switch element according to user suggestion 
			 * 
			 */
			switch(control_mode){
				case CLOSED:{
					
					if(v_geyser.getInternalTemp() <= default_setpointLow){
						v_geyser.setElementState(true);
					}
					else if(v_geyser.getInternalTemp() >= default_setpointHigh){
						v_geyser.setElementState(false);
					}
					
					break;
				}
				case OPEN:{
					 // If geyser is in good health:
					v_geyser.setElementState(element_request);

					break;
				}
			}
		
			/* -----------------------------------------------------------------------------*/
			
			/*
			 * API command service:
			 	* Check if new command is available in Command-queue.
			 	* Evaluates command integrity.
			 	* Respond accordingly:
			 		* Change system parameters,
			 		* or post data in Reply-queue.
			 		* Subtract one from time_to_live for control_mode.OPEN if appropriate.
			 	BUSINESS RULE: Each command must result in a reply. Even if it is just 'Ack'.
			 */

			if(!commandQueue.isEmpty()){
				String command = commandQueue.pop(); //Pop new command off queue
				
				replyQueue.push("Command ack: " + command); //Echo command back to user
				
				System.out.println("Command handled.");
			}
			
			
			/* -----------------------------------------------------------------------------*/

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	// --------------------------- LISTENERS --------------------------
	 
	/* 
	 * API communications Command-queue reader and Reply-queue writer
	 	* Listens for in-bound data and puts it in the Command-queue
	 	* Writes any data in the Reply-queue to client.
	 	* Normal Queue flags should be used to coordinate the process. e.g. isEmpty(), isFull() etc. 
	 	* This is the only part that is protocol dependent. 
 		*
 		* 
 		* TCP server thread:
 			* Listens for client. (SINGULAR, meaning firmware is monogamous)
 			* Reads packets into temporary buffer until '\n' is reached
 			* Puts content of buffer in CommandQueue
		*
		* TCP client-proxy thread:
			* Waits for content on the ReplyQueue
			* Writes data out to client.
	 */
	
}


/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */