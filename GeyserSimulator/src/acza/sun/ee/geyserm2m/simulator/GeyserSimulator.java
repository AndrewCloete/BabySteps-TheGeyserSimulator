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
	Control_mode[] control_mode = Control_mode.values();
	private boolean element_request = false;
	
	
	//Internal parameters
	private static final int default_setpointHigh = 55;
	private static final int default_setpointLow = 45;
	
	private final int API_TIMEOUT_RESET = 100;
	private int api_timeout;
	private boolean api_active = false; 
	
	private final int PORT = 3000;

	/*
	 * Represents the main entry point of firmware.
	 */
	public static void main(String [] args)
	{
		System.out.println("Baby steps");
		
		/*
		 * Initialize
			 * Thread Listeners that checks bursts/power failures "Interrupt routine"
			 * Initialize TCP handlers 
			 	* that captures data and packs it in the in-bound buffer
			 	* transmits data present in the out-bound buffer.
			 * Start api_timeout count down thread "Interrupt timer routine"
			 * Set Control_mode to default
			 * Reads persistence file 
			 * Initialize VirtualGeyser(usage event file);
			 	* Calculates time elapsed between fail-over and restart.
			 	* Fast-forwards virtual geyser up to current time.
		 */
		
		
		/*	MAIN PROGRAM LOOP
		 * 
			 * Control: switch case(Control_mode)
				 * CLOSED:
				 * 		- Switch element according to internal temperature, between default dead-bands.
				 * OPEN:
				 * 		- Switch element according to user suggestion 
			 * 
			 * Step virtual geyser.
			 * Persist geyser data (later though) 
			 * 
			 * 
			 * API Command server:
			 	* Check if new command is available in in-bound buffer.
			 	* Resets ack flag.
			 	* Evaluates command integrity.
			 	* Respond accordingly:
			 		* Set system parameters,
			 		* or post reply data in out-bound buffer.
			 		* 
		 * 
		 * wait for 5 second mark using real time.
		 * (The simulation period has to be exact since the usage event file is exact)
		 * 
		 * LOOP
		 */
		
	}
	
	// ---------------------------- THREADS ---------------------------
	
	/* 
	 * API timeout 
		 * Thread starts when OPEN-LOOP CONTROL message is passed.
		 * (Every time TCP activity occurs, api_timeout = API_TIMEOUT_RESET.)
		 * If time runs out --> control_mode = CLOSED and thread dies;
		 * 
	 */
	
	// --------------------------- LISTENERS --------------------------
	 
	/*
	 * TCP reader and packer
	 	* Listens for in-bound packets and packs buffer
	 	* Raises ack flag when new commend is received
	 	* 
	 * TCP writer
	 	* Writes out-bound databuffer to TCP client
	 */
	
}


/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */