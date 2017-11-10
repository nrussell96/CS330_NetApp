//NATE RUSSELL CS330.801
package ycp.cs330.UDPpinger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

public class PingClient {
	
	public static void main(String[] args) throws Exception{
		
		// check if args valid
		if (args.length != 2) {
	         System.out.println("Invalid arguments, must be server name and port");
	         return;
	    }
		
		// get server to ping and port number, and initialize seq number
		int port = Integer.parseInt(args[1]); //port arg
		System.out.println(port);
		InetAddress server = InetAddress.getByName(args[0]); //ip arg
		int seq = 0; //sequence begins at 0
		
		//vals for recording ping stats
		//NOTE: min and max RTT are incorrect as I cant figure them out
		int numReceived = 0;
		int numLost = 0;
		int avgRTT = 0;
		int currentMin = 0;
		int currentMax = 0;
		
		// create the datagram socket to send/receive UDP packets
		DatagramSocket socket = new DatagramSocket();
		
		
		while(seq < 15) {
			// get time to put in to udp packet
			Date date = new Date();
			long sentTime = date.getTime();
			
			// create contents of packet, and put into byte array
			String contents = "PING " + seq + ": " + " \n";
			byte[] buffer = new byte[1024];
			buffer = contents.getBytes();
			
			// create packet to send 
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, server, port);
			// send the packet to the server
			socket.send(packet);
			
			// attempt to receive packet from server
			try {
				socket.setSoTimeout(1000);
				// create packet to receive message from server
				DatagramPacket serverPacket = new DatagramPacket(new byte[1024], 1024);
				//attempt to receive message from server
				socket.receive(serverPacket);
				// get time of packet arrival
				date = new Date();
				//time packet received
				long receiveTime = date.getTime();
				
				//supposed to keep track of min and max RTT's, but gets erratic vals
				if(receiveTime < currentMin)currentMin = (int)receiveTime;
				if(receiveTime > currentMax)currentMax = (int)receiveTime;
				
				printData(serverPacket, sentTime, receiveTime);
				numReceived++;
				avgRTT += (receiveTime - sentTime);
			} catch (SocketTimeoutException e) {
				//Packet timed out, print out error message
				System.out.println("PING " + seq + " was lost");
				numLost++;
			}
			//get seq of next packet
			seq++;
			//wait 2 sec before sending 
			Thread.sleep(2000);	
		}
		//for printing stats at the end of ping list
		System.out.println("Min RTT: " + currentMin + " Max RTT: " + currentMax + " Avg RTT: " + (avgRTT / numReceived));
		System.out.println(numReceived + " Replies, " + numLost + " Lost");
	}
	
	// taken from PingServer
	private static void printData(DatagramPacket serverPacket, long sentTime, long receiveTime) throws Exception {
		// Obtain references to the packet's array of bytes.
	      byte[] buf = serverPacket.getData();

	      // Wrap the bytes in a byte array input stream,
	      // so that you can read the data as a stream of bytes.
	      ByteArrayInputStream bais = new ByteArrayInputStream(buf);

	      // Wrap the byte array output stream in an input stream reader,
	      // so you can read the data as a stream of characters.
	      InputStreamReader isr = new InputStreamReader(bais);

	      // Wrap the input stream reader in a buffered reader,
	      // so you can read the character data a line at a time.
	      // (A line is a sequence of chars terminated by any combination of \r and \n.) 
	      BufferedReader br = new BufferedReader(isr);

	      // The message data is contained in a single line, so read this line.
	      String line = br.readLine();

	      // Print host address and data received from it.
	      System.out.println(new String(line) + "RTT = " + (receiveTime-sentTime));
	}
}