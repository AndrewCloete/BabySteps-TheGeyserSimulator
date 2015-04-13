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
import org.json.simple.JSONObject;

public class GeyserSimulator {
	
	//API Read-only parameters
	private char critical_flags = 0;

	//API Read-Write parameters
	private enum Control_mode {CLOSED, OPEN}
	private static Control_mode control_mode;
	private static boolean element_request = false;
	
	
	//Internal parameters
	private static final float default_setpointHigh = 50;
	private static final float default_setpointLow = 45;
	
	private static LinkedList<String> commandQueue = new LinkedList<String>();
	private static LinkedList<String> replyQueue = new LinkedList<String>();
	
	private static int PORT = 3000;
	
	private static int open_ttl = 0;
	
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
		
		control_mode = Control_mode.CLOSED; //Initialize default control mode
		
		/* MAIN PROGRAM LOOP:
			 * Control routine:	Lowest level hardware controller
			 * API Command service: Process commands from API
			 * 
			 * sleep for about a second.
			 * LOOP forever
		 */
		
		while(true){

			//Debug messages
			System.out.println("Control mode: " + control_mode);
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
					
					open_ttl -= 1;		//Decrement open-loop control time-to-live
					if(open_ttl <= 0)	//If ttl runs out, switch back to closed-loop control
						control_mode = Control_mode.CLOSED;

					break;
				}
			}
		
			/* -----------------------------------------------------------------------------*/
			
			/*
			 * API command service:
			 	* Check if new command is available in Command-queue.
			 	* Respond accordingly:
			 		* Evaluate command integrity. 
			 		* Change system parameters,
			 		* or post data in Reply-queue.
			 	BUSINESS RULE: Each command must result in a reply. Even if it is just 'Ack'.
			 */

			if(!commandQueue.isEmpty()){
				String command = commandQueue.pop(); 		//Pop new command off queue
				String response = "Err: Invalid command";	//Default response;
				
				//Simple command handler
				if(command.equals("open")){
					open_ttl = 5;							//Reset time-to-live
					control_mode = Control_mode.OPEN;
					response = "Ack: Switching to open-loop control mode";
				}
				else if(command.equals("elementon")){
					if(control_mode == Control_mode.OPEN){
						open_ttl = 5;
						element_request = true;
						response = "Ack: Element on requested.";
					}
					else{
						response = "Err: System under closed-loop control.";
					}
				}
				else if(command.equals("elementoff")){
					if(control_mode == Control_mode.OPEN){
						open_ttl = 5;
						element_request = false;
						response = "Ack: Element off requested.";
					}
					else{
						response = "Err: System under closed-loop control.";
					}
				}
				else if(command.equals("get")){
					JSONObject geyserdata = new JSONObject();
					
					geyserdata.put("InternalTemp", v_geyser.getInternalTemp());
					geyserdata.put("ElementState", v_geyser.getElementState());
					geyserdata.put("ControlMode", control_mode.toString());
					response = geyserdata.toString();
					
				}
				
				replyQueue.push(response);
				
				/*
				 * To-do: Command handler
				 	* Create as separate private method
				 	* Use enum object for command verifcation and swithcing 
				 */
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
 			* Reads a line from client.
 			* Puts content of buffer in CommandQueue
 			* Waits for content on the ReplyQueue
 			* Writes data out to client.
	 */
	
}


/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */