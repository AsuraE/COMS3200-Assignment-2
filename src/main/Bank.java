package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Bank {
	
	// Define timeout period (in ms) 
	private static final int TIMEOUT = 10000;
	// Define amount of times to retry
	private static final int RETRIES = 5;
	// 0 = no packets arrive, 1 = all packets arrive
	private static final double PACKET_LOSS_SIM = 0.75;
	// Send and receive buffers
	private static byte[] receiveData = new byte[1024];
	private static byte[] sendData = new byte[1024];
	// Datagram socket 
	private static DatagramSocket serverSocket; 
	// Datagram packet to receive
	private static DatagramPacket receivePacket;
	// Datagram packet to send
	private static DatagramPacket sendPacket;
	
	public static void main(String args[]) throws Exception {
		
		// Port number for Bank
		int bankPort = 0;
		// Port number for NameServer
		int nsPort = 0;
		// String to store reply from servers
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
		serverSocket = new DatagramSocket(bankPort);
		
		System.out.println("Bank waiting for incoming connections...");
		
		// Register with name server
		// Set server's IP address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// Create data to be sent
		sendData = ("register " + "Bank " + "127.0.0.1 "	+ bankPort)
				.getBytes();
		sendPacket = new DatagramPacket(sendData, 
				sendData.length, IPAddress, nsPort);
		
		// Send message while simulating packet loss
		// If our send is unsuccessful, end the process
		if (!simulatePacketLoss()) {
			serverSocket.close();
			System.exit(1);
		}
		
		// Request sent at this point, registered with nameserver
		
		// Wait for a reply from nameserver
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);

		// Send ACK
		ack(IPAddress, nsPort);
		
		// Convert reply to a String
		reply = new String(receivePacket.getData());
		reply = reply.trim();

		// Make sure no error occurred
        if (reply.equalsIgnoreCase("Error")) {
        	System.err.println("Bank registration to NameServer failed");
        	serverSocket.close();
        	System.exit(1);
        } 
        
        // Now that we've registered with nameserver, start the bank's stuff
		while(true) {
			// Wait for bank to receive a new packet
			receiveData = new byte[1024];
			receivePacket = new DatagramPacket(receiveData,	receiveData.length);
			serverSocket.receive(receivePacket);
			
			// Grab IPAddress and Port to reply to
			InetAddress replyIPAddress = receivePacket.getAddress();
			int replyPort = receivePacket.getPort();
			
			// Send ACK
			ack(replyIPAddress, replyPort);
			
			// Convert reply to a String
			reply = new String(receivePacket.getData());
			reply = reply.trim();
			
	    	// Input will be of format "item-id item-price credit-card-number"
	    	String split[] = reply.split(" ");
	    	
	    	// If item-id is odd, we sell. If even, we don't
	    	long temp = 0l;
	    	try {
	    		temp = Long.parseLong(split[0]);
	    	} catch (NumberFormatException e) {
	    		System.err.println("Invalid item-id");
	    	}
	    	
	    	if (temp % 2 == 1) {
	    		// Reply to connection that sale is ok
	    		sendData = "0".getBytes();
	    		sendPacket = new DatagramPacket(sendData, sendData.length, 
	    				replyIPAddress, replyPort);
	    			
	    		// Send message while simulating packet loss
	    		// If our send is unsuccessful, break out of loop
	    		if (!simulatePacketLoss()) {
	    			break;
	    		}
	    		
	    		System.out.println(split[0] + " OK");
	    	} else {
	    		// Reply to connection that sale is not ok
	    		sendData = "1".getBytes();
	    		sendPacket = new DatagramPacket(sendData, sendData.length, 
	    				replyIPAddress, replyPort);
	    		
	    		// Send message while simulating packet loss
	    		// If our send is unsuccessful, break out of loop
	    		if (!simulatePacketLoss()) {
	    			break;
	    		}
	    		
	    		System.out.println(split[0] + " NOT OK");
		    }
		}
		serverSocket.close();
	}	
	
	/**
	 * Method to simulate packet loss and send a packet. Returns true or false
	 * depending on whether server was reachable or not
	 * 
	 * @param:  none
	 * @return: boolean
	 * @throws: Excpetion
	 */
	private static boolean simulatePacketLoss() throws Exception {
		// Simulate packet loss
		double x = Math.random();
		if (x < PACKET_LOSS_SIM) {
			serverSocket.send(sendPacket);
		}
		
		// Set timeout to defined amount of time
		serverSocket.setSoTimeout(TIMEOUT);
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		// Wait for ACK
		int attempts;
		for (attempts = 0; attempts < RETRIES; attempts++) {
			try {
				serverSocket.receive(receivePacket);
				break;
			} catch (SocketTimeoutException se) {
				// ACK not received, resend packet and again,
				// simulate packet loss
				x = Math.random();
    			if (x < PACKET_LOSS_SIM) {
    				serverSocket.send(sendPacket);
    			}
			}
		}
		
		// Set timeout back to infinite
		serverSocket.setSoTimeout(0);
		
		// If no ACK response comes after RETRIES number of times,
		// assume server is offline and end
		if (attempts >= RETRIES) {
			System.err.println("No response, server offline.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Method to send basic ACK response
	 * 
	 * @param replyIPAddress
	 * @param replyPort
	 * @throws Exception
	 */
	private static void ack(InetAddress replyIPAddress, int replyPort) 
			throws Exception {
		sendData = "ACK".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, 
				replyIPAddress, replyPort);
		serverSocket.send(sendPacket);
	}
}
