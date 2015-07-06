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
	private static final String persistence_filename = "/home/andrew/git/GeyserSimulator/GeyserSimulator/src/resources/persist.xml";
	
	private float internal_temp;
	private float outlet_temp;
	private float inlet_temp;
	private float ambient_temp;
	
	private boolean element_state;
	
	private static final int T_DELTA = 60; //Period step size must be constant.
	
	private Map<Long, Integer> water_usage_map; //<Timestamp, UsageAmount>
	
	private boolean virtual_geyser_initialized; //Flag to inform simulator that VirtualGeyser is ready
	
	public VirtualGeyser(){
		/*
		 * Get usage event map from file
		 * 
		 * Check if persistence data is available
		 	* NO - Set initial values to default values
		 	* YES - Set initial values to last known persistence values
		 	* 		Calculates time elapsed between fail-over and restart
		 	* 		Fast-forwards virtual geyser up to current time.  
		 */
		
		this.virtual_geyser_initialized = false;
		
		this.water_usage_map = getWaterUsageMap(usage_filename);
		
		if(this.setStateToLastPersistence(persistence_filename) == false){
			this.internal_temp = (float)42;
			this.ambient_temp = 25;
			this.element_state = false;
		}
			
		
		this.virtual_geyser_initialized = true;
	}
	
	/*
	 * Calculates new geyser values using the current values, and a usage amount.
	 * The calculation assumes a constant delta_time (period) and is independent of time.
	 */
	private void step(int water_usage_amount){
		//Implement Philips' model here
		
		//Simplified model for testing of system
		if(water_usage_amount <= 0)
			this.internal_temp = this.internal_temp - (this.internal_temp - this.ambient_temp)/250;	//Heat loss due to dissipation 
		else{
			this.internal_temp -= 0.05*water_usage_amount; //Heat loss due to water leaving the system
		}
		
		if(this.element_state == true){
			this.internal_temp += 0.2;	//Heat gained due to element being on
		}
		
		System.out.println("Virtual geyser time-step occured. Elementstate: " + this.element_state);
		
	}
	
	@Override
	public void run() {
		/* 
		 * Step every minute: (The simulation period has to be exact since the usage event file is exact)
		 * 
		 * Calculate geyser state for this step
		 	* water_usage_amount = this.water_usage_map.get(current_time)
		 	* step(water_usage_amount);
		 * 
		 * Persist state to file
		 * 
		 * Must include randomized failures.
		 */
		
		//Hard coded for testing. Should be: current_time = "Date.currentTime.minutes()".
		while(true){
			
			try {
				Thread.sleep(10000); //NB: Should be 60 seconds. (But thats too long for debugging)
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			long current_time = 1418594253;	
			
			int water_usage_amount = this.water_usage_map.get(current_time);
			this.step(water_usage_amount);
		}
	}
	
	private void fastForward(long start_time, long end_time){
		
		/* PSEUDO
		 * for(t = start_time, t <= end_time, t++){
		 * 		water_usage_amount = this.water_usage_map.get(t);
		 * 		this.step(usage);
		 * }		
		 */
	}
	
	//---------------  ACCESS METHODS -----------------
	//(In the real world, these methods equate to reading or writing registers, A/D values, IO states etc.)

	public boolean getElementState(){
		return this.element_state;
	}
	
	public void setElementState(boolean state){
		this.element_state = state;
	}
	
	public float getInternalTemp(){
		return this.internal_temp;
	}
	
	public boolean geyserReady(){
		return this.virtual_geyser_initialized;
	}
	
	//--------------------- Utility methods -----------------------
	//(no real world equivalents)
	
	//Reads time-stamped usage event file into memory as MAP
	private Map<Long, Integer> getWaterUsageMap(String usage_filename){
		
		/*
		 * Open and read usage file
		 * Populate Map with values from file;
		 */
		
		Map<Long, Integer> usage_map = new HashMap<Long, Integer>();
		
		//Single hard-coded usage event for testing
		usage_map.put((long) 1418594253, 0);
		
		return usage_map;
	}
	
	private boolean setStateToLastPersistence(String persistence_filename){
		
		/* PSEUDO
		 * Open persistence_filename
		 	*if(Does not exit){return false}
		 	*else { 
		 	*	this.internal_temp = readfile.xx
		 	*	this.ambient_temp = readfile.yy
		 	*}
		 	*
		 	* file.close();
		 */
		
		return false; //Hard coded for testing;
		
	}

}

/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */