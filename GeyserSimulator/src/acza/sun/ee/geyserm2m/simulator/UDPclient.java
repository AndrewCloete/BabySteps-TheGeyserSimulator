/* --------------------------------------------------------------------------------------------------------
 * DATE:	27 May 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: 
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.geyserm2m.simulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPclient {
	
	private DatagramSocket socket;
	private InetAddress NIP_IP_ADR;
	private int PORT;
	private final int PACKETSIZE;
	
	
	public UDPclient() throws UnknownHostException, SocketException{
		this.NIP_IP_ADR = InetAddress.getByName("52.10.236.177");
		this.PORT = 3535;
		this.PACKETSIZE = 1024;
		this.socket = new DatagramSocket();
	}
	
	public UDPclient(String ip_address, int port, int packet_size) throws UnknownHostException, SocketException{
		this.NIP_IP_ADR = InetAddress.getByName(ip_address);
		this.PORT = port;
		this.PACKETSIZE = packet_size;
		this.socket = new DatagramSocket();
	}
	
	
	public String sendPacket(String msg){
		// Construct the datagram packet
		byte [] msg_data = msg.getBytes() ;
		DatagramPacket packet = new DatagramPacket( msg_data, msg_data.length, this.NIP_IP_ADR, this.PORT ) ;


		try {
			// Send it
			socket.send( packet ) ;

			// Set a receive timeout, 2000 milliseconds
			socket.setSoTimeout( 2000 ) ;

			// Prepare the packet for receive
			packet.setData( new byte[PACKETSIZE] ) ;

			// Wait for a response from the server
			socket.receive( packet ) ;
			
			//Return reply
			return new String(packet.getData());
			
		} catch (SocketException e) {
			e.printStackTrace();
			return "Error. TODO: Add detail.";
		} catch (IOException e) {
			e.printStackTrace();
			return "Error. TODO: Add detail.";
		}
	}
}

