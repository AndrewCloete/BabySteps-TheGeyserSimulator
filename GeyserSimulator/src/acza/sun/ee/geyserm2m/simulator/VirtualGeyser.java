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

public class VirtualGeyser {
	
	private int internal_temp;
	private int outlet_temp;
	private int inlet_temp;
	private int ambient_temp;
	
	private boolean element_state;
	private boolean valve_state;
	
	private int t_delta; //Period step size must be constant?
	
	
	public VirtualGeyser(/* UsageEventFile.csv */){
		//Reads file into memory
	}
	
	public void step(){
		
		// Must include randomized failures.
	}
	
	
	//---------------  ACCESS METHODS -----------------
	//(In the real world, these methods equate to reading or writing registers, A/D values, IO states etc.)
	
	public boolean getElementState(){
		return this.element_state;
	}
	
	
	

}

/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */