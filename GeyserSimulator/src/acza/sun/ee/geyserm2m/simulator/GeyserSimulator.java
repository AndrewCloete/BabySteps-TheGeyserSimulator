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


import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GeyserSimulator {
	
	//API Read-only parameters
	private char critical_flags = 0;

	//API Read-Write parameters
	private enum ControlMode {CLOSED, OPEN}
	private static ControlMode control_mode;
	private enum ElementRequest {ON, OFF, UNKNOWN, AUTO};
	private static ElementRequest element_request = ElementRequest.UNKNOWN;
	
	
	//Internal parameters
	private static final float default_setpointHigh = 50;
	private static final float default_setpointLow = 45;
	
	private static UDPclient udp_client;
	private static long GEYSER_ID;
	
	
	private static final int CONTROL_PERIOD = 5; //In seconds
	
	private static int open_ttl = 0;
	private static final int TTL_RESET = 60; //Time to live is TTL_RESET * CONTROL_PERIOD seconds
	
	/*
	 * Represents the main entry point of firmware.
	 */
	public static void main(String [] args)
	{	
		
		// ---------------------- Sanity checking of command line arguments -------------------------------------------
		if( args.length != 3 )
		{
			System.out.println( "Usage: <NSCL IP address>  <UDPServer port>  <NIP ID>" ) ;
			return;
		}

		final int UDP_PORT;
		try{
			UDP_PORT = Integer.parseInt( args[1] ); // Convert the argument to ensure that is it valid
		}catch (NumberFormatException e){
			System.out.println( "UDP port invalid." ) ;
			return;
		}
		
		try{
			udp_client = new UDPclient(args[0], UDP_PORT, 1024);
		} catch (UnknownHostException e){
			System.out.println("Invalid IP address.");
			return;
		} catch (SocketException e){
			System.out.println("Unable to create socket.");
			return;
		}
		
		
		try{
			GEYSER_ID = new Long(args[2]);
		} catch (NumberFormatException e){
			System.out.println( "Geyser ID invalid." ) ;
			return;
		}

		//---------------------------------------------------------------------------------------------------------------
		
		
		/*
		 * Initialise
			 * Initialise VirtualGeyser;
			 	* Wait for geyserReady
			 	* start VirtualGeyserThread
			 * Set Control_mode to default 
		 */

		System.out.println("Starting Virtual Geyser...");
		VirtualGeyser v_geyser = new VirtualGeyser();
		while(!v_geyser.geyserReady());
		Thread virtualgeyser_thread = new Thread(v_geyser);
		virtualgeyser_thread.start();
		System.out.println("Virtual Geyser started.\n");
		
		control_mode = ControlMode.CLOSED; //Initialize default control mode
		
		/* MAIN PROGRAM LOOP:
			 * Control routine:	Lowest level hardware controller
			 * API Command service: Process commands from API
			 * 
			 * sleep for about a second.
			 * LOOP forever
		 */
		
		while(true){

			//Take measurements
			float internal_temp = v_geyser.getInternalTemp();
			boolean elemet_state = v_geyser.getElementState();
			
			
			//Debug messages
			System.out.println("Control mode: " + control_mode);
			System.out.println("T_internal: " + internal_temp);
			System.out.println("Element state: " + elemet_state);
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

				if(internal_temp <= default_setpointLow){
					v_geyser.setElementState(true);
				}
				else if(internal_temp >= default_setpointHigh){
					v_geyser.setElementState(false);
				}

				break;
			}
			case OPEN:{
				// If geyser is in good health:
				switch(element_request){
				case ON:{
					v_geyser.setElementState(true);
					break;
				}

				case OFF:{
					v_geyser.setElementState(false);
					break;
				}
				default:
					break;
				}

				open_ttl -= 1;		//Decrement open-loop control time-to-live
				if(open_ttl <= 0)	//If ttl runs out, switch back to closed-loop control
					control_mode = ControlMode.CLOSED;

				break;
			}
			}
		
			/* -----------------------------------------------------------------------------*/
			
			//Send system info and measurements, and receive new control data.
			String msg = "{\"id\":"+ GEYSER_ID + ", \"t1\":"+ internal_temp +", \"e\":\""+ elemet_state +"\"}";
			System.out.println("To server: " + msg);
			
			String recieve = udp_client.sendPacket(msg);
			System.out.println("From server: " + recieve);
			
			//Parse 
			String new_element_state = (String)getValueFromJSON("e", recieve.trim());
			if(new_element_state != null){
				if(new_element_state.equalsIgnoreCase("ON")){	
					element_request = ElementRequest.ON;
					control_mode = ControlMode.OPEN;
					open_ttl = TTL_RESET;
				}
				else if(new_element_state.equalsIgnoreCase("OFF")) {
					element_request = ElementRequest.OFF;
					control_mode = ControlMode.OPEN;
					open_ttl = TTL_RESET;
				}
				else if(new_element_state.equalsIgnoreCase("AUTO")){
					element_request = ElementRequest.AUTO;
					control_mode = ControlMode.CLOSED;
				}
				else{
					element_request = ElementRequest.UNKNOWN;
					control_mode = ControlMode.CLOSED;
				}
			}
			
			/* -----------------------------------------------------------------------------*/
			
			//Firmware period is controlled here.  
			try {
				Thread.sleep(CONTROL_PERIOD*1000);	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	private static Object getValueFromJSON(String key, String JSON){

		JSONParser parser=new JSONParser();
		try{
			Object obj = parser.parse(JSON);
			JSONArray array = new JSONArray();
			array.add(obj);	
			JSONObject jobj = (JSONObject)array.get(0);

			return jobj.get(key);

		}catch(ParseException pe){
			System.err.println("JSON parse exeption at position: " + pe.getPosition() + " : " + pe);
			return "Error";
		}
	}
	

}


/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */