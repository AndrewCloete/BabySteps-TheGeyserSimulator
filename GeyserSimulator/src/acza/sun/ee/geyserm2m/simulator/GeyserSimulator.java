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
	
	private final int API_TIMEOUT_RESET = 100;
	private int api_timeout;
	private boolean api_active = false; 
	
	private final int PORT = 3000;

	/*
	 * Represents the main entry point of firmware.
	 */
	public static void main(String [] args)
	{
		System.out.println("Project: Baby steps.\n");
		
		/*
		 * Initialize
			 * Thread Listeners that checks bursts/power failures "Interrupt routine"
			 * Initialize communications handlers 
			 	* that captures data and packs it in the in-bound buffer
			 	* transmits data present in the out-bound buffer.
			 * Start api_timeout count down thread "Interrupt timer routine"
			 * Set Control_mode to default
			 * Initialize VirtualGeyser;
			 	* Wait for geyserReady
			 	* start VirtualGeyserThread
		 */
		
		VirtualGeyser v_geyser = new VirtualGeyser();
		
		while(!v_geyser.geyserReady());
		Thread virtualgeyser_thread = new Thread(v_geyser);
		virtualgeyser_thread.start();
		
		
		
		/*	MAIN PROGRAM LOOP
		 * 
			 * Control: switch case(Control_mode)
				 * CLOSED:
				 * 		- Switch element according to internal temperature, between default dead-bands.
				 * OPEN:
				 * 		- Switch element according to user suggestion 
			 * 
			 * 
			 * API Command server:
			 	* Check if new command is available in in-bound buffer (Check ack flag).
			 	* Resets ack flag.
			 	* Evaluates command integrity.
			 	* Respond accordingly:
			 		* Set system parameters,
			 		* or post reply data in out-bound buffer.
			 		* 
		 * 
		 * sleep for about a second.
		 * 
		 * LOOP
		 */
		
		//Hard coded for testing
		control_mode = Control_mode.CLOSED;
		
		while(true){

			System.out.println("Control mode: " + Control_mode.CLOSED);
			System.out.println("T_internal: " + v_geyser.getInternalTemp());
			System.out.println("Element state: " + v_geyser.getElementState());
			System.out.println();
			
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
		

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	
	
	// ---------------------------- THREADS ---------------------------
	
	/* 
	 * API timeout 
		 * Thread starts when OPEN-LOOP CONTROL message is passed.
		 * (Every time API activity occurs, api_timeout = API_TIMEOUT_RESET.)
		 * (NB: Keep this protocol INDEPENDENT. E.g. TCP is "permanently" connected i.e. false positive)
		 * If time runs out --> control_mode = CLOSED and thread dies;
		 *
 	 *
	 */
	
	// --------------------------- LISTENERS --------------------------
	 
	/* 
	 * API communications reader and packer
	 	* Listens for in-bound packets and packs buffer
	 	* Raises ack flag when new commend is received
	 	* 
	 */
	
}


/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */