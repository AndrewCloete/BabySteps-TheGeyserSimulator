/* --------------------------------------------------------------------------------------------------------
 * DATE:	11 Apr 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Utility methods for geyser simulator
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserm2m.simulator;

import java.io.*;

public class Utils {
	
	public static void readPulseDataFromFile(String filename){
		
		String file_data = new String();
		
		//Read file content to file_data
		try {
			Reader ir = new InputStreamReader(new FileInputStream(filename));
			BufferedReader in = new BufferedReader(ir);
			
			String buff;
			while((buff = in.readLine()) != null){
				file_data = buff;
			}
			in.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		
		String[] parts = file_data.split("-");
		
		System.out.println(parts[0]);
		System.out.println(parts[1]);
		
		String[] ts_str = parts[0].split(",");
		String[] vs_str = parts[1].split(",");
		
		//ToDo - Make this a MAP
		long[] ts = new long[ts_str.length];
		int[] vs = new int[vs_str.length];

		for (int i = 0; i < ts_str.length; i++) {
		    try {
		    	ts[i] = Long.parseLong(ts_str[i]);
		    } catch (NumberFormatException nfe) {};
		}
		
		for (int i = 0; i < vs_str.length; i++) {
		    try {
		    	vs[i] = Integer.parseInt(vs_str[i]);
		    } catch (NumberFormatException nfe) {};
		}
		
		
		try {
			Writer or = new OutputStreamWriter(new FileOutputStream("/home/andrew/git/GeyserSimulator/GeyserSimulator/src/resources/pulsefile_formattet.txt"));
			BufferedWriter out = new BufferedWriter(or);
			
			for (int i = 0; i < ts_str.length; i++){
				out.write(ts_str[i] + "," + vs_str[i] + "\n");
			}
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
			
		
	}
	

}

/*
 * ---------------------------------------------------------------------------------------------------------
 * NOTES:
 * ---------------------------------------------------------------------------------------------------------
 */