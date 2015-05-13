package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Bank {

	public static void main(String args[]) throws Exception {
				
		int bankPort = 0;
		int nsPort = 0;
		DatagramPacket receivePacket;
		String reply;
		
		// If there are more than 3 arguments
		if (args.length != 2) {
			System.err.println("Invalid command line arguments for Bank");
			System.exit(1);
		}
		
		// Parse bankPort
		try {
			bankPort = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.err.println("Bank unable to listen on given port");
			System.exit(1);
		}
		
		// Parse nsPort
		try {
			nsPort = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.err.println("Bank registration to NameServer failed");
			System.exit(1);
		}
		
		// Construct datagram socket with our given port for bank
		DatagramSocket serverSocket = new DatagramSocket(bankPort);
		
		// Set buffers
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		System.out.println("Bank waiting for incoming connections "
        		+ "....");
		
		// Register with name server
		DatagramSocket clientSocket = new DatagramSocket();
		// Set server's IP address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// Create data to be sent
		sendData = ("register " + "Bank " + "127.0.0.1" + " " 
				+ bankPort).getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, 
				sendData.length, IPAddress, nsPort);
		clientSocket.send(sendPacket);
		// Request sent at this point, registered with nameserver
		
		// Wait for a reply from nameserver
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		// Convert reply to a String
		reply = new String(receivePacket.getData());

		// Make sure no error occurred
        if (reply.equalsIgnoreCase("Error")) {
        	System.err.println("Bank registration to NameServer failed");
        	System.exit(1);
        } 
        
        // Now that we've registered with nameserver, start the bank's stuff
		while(true) {
			// Wait for bank to receive a new packet
			receivePacket = new DatagramPacket(receiveData,	receiveData.length);
			clientSocket.receive(receivePacket);
			
			InetAddress replyIPAddress = receivePacket.getAddress();
			int replyPort = receivePacket.getPort();
			
			// Convert reply to a String
			reply = new String(receivePacket.getData());
			
	    	// input will be of format "item-id item-price credit-card-number"
	    	String split[] = reply.split(" ");
	    	
	    	// If item-id is odd, we sell. If even, we don't
	    	long temp = 0l;
	    	try {
	    		temp = Long.parseLong(split[0]);
	    	} catch (NumberFormatException e) {
	    		e.printStackTrace();
	    	}
	    	
	    	if (temp % 2 == 1) {
	    		// Reply to connection that sale is ok
	    		sendData = "0".getBytes();
	    		sendPacket = new DatagramPacket(sendData, sendData.length, 
	    				replyIPAddress, replyPort);
				serverSocket.send(sendPacket);
	    		System.out.println(split[0] + " OK");
	    	} else {
	    		// Reply to connection that sale is not ok
	    		sendData = "1".getBytes();
	    		sendPacket = new DatagramPacket(sendData, sendData.length, 
	    				replyIPAddress, replyPort);
				serverSocket.send(sendPacket);
	    		System.out.println(split[0] + " NOT OK");
		    }
		}
	}
}
