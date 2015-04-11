/* --------------------------------------------------------------------------------------------------------
 * DATE:	10 Apr 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: This software contains the theoretical model of a geyser and provides access methods that 
 * 				returns virtual sensor information.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: Emulate hardware sensors and actuators
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserm2m.simulator;

import java.util.Map;
import java.util.HashMap;


public class VirtualGeyser implements Runnable {
	
	private static final String usage_filename = "/home/andrew/git/GeyserSimulator/GeyserSimulator/src/resources/usage.txt";
	private static final String persistence_filename = "/home/andrew/git/GeyserSimulator/GeyserSimulator/src/resources/persist.txt";
	
	private int internal_temp;
	private int outlet_temp;
	private int inlet_temp;
	private int ambient_temp;
	
	private boolean element_state;
	private boolean valve_state;
	
	private int t_delta; //Period step size must be constant? YES
	
	private Map<Long, Integer> water_usage_map; //<Timestamp, UsageAmount>
	
	
	public VirtualGeyser(){
		/*
		 * 
		 * Check if persistence data is available
		 	* NO - set state to default initial values
		 	* YES - Calculates time elapsed between fail-over and restart
		 	* 		Fast-forwards virtual geyser up to current time.
		 */
		
	}
	
	@Override
	public void run() {
		/* 
		 * Must run exactly every minute
		 * 
		 * Calculate next step
		 	* Check water usage for this minute, using current real-time.
		 * 
		 * Persist state to file
		 * 
		 * Must include randomized failures.
		 */
		
	}
	
	//---------------  ACCESS METHODS -----------------
	//(In the real world, these methods equate to reading or writing registers, A/D values, IO states etc.)

	public boolean getElementState(){
		return this.element_state;
	}
	
	
	//--------------------- Utility methods -----------------------
	//(no real world equivalents)
	
	//Reads time-stamped usage event file into memory as MAP
	private Map<Long, Integer> getWaterUsageMap(String usage_filename){
		
		/*
		 * Open and read file
		 * Populate Map with values;
		 */
		
		Map<Long, Integer> usage_map = new HashMap<Long, Integer>();
		usage_map.put((long) 1418594253, 0);
		
		return usage_map;
	}

}

/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */