package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Bank {
	
	// Define timeout period (in ms) and retry limit
	private static final int TIMEOUT = 5000;
	private static final int RETRIES = 3;
	// 0 = no packets arrive, 1 = all packets arrive
	private static final int PACKET_LOSS_SIM = 1;
	
	public static void main(String args[]) throws Exception {
				
		int bankPort = 0;
		int nsPort = 0;
		DatagramPacket receivePacket;
		String reply;
		double x;
		int attempts;
		
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
		// Set server's IP address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// Create data to be sent
		sendData = ("register " + "Bank " + "127.0.0.1 "	+ bankPort)
				.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, 
				sendData.length, IPAddress, nsPort);
		// Simulate packet loss
		x = Math.random();
		if (x < PACKET_LOSS_SIM) {
			serverSocket.send(sendPacket);
		}

		// Set timeout to defined amount of time
		serverSocket.setSoTimeout(TIMEOUT);
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, 
				receiveData.length);

		// Wait for ACK
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
			System.err.println("Bank registration to NameServer failed due to no ACK");
			System.exit(1);
		}
		
		// Request sent at this point, registered with nameserver
		
		// Wait for a reply from nameserver
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);

		// Send ACK
		sendData = "ACK".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, 
		        IPAddress, nsPort);
		serverSocket.send(sendPacket);
		
		// Convert reply to a String
		reply = new String(receivePacket.getData());
		reply = reply.trim();

		// Make sure no error occurred
        if (reply.equalsIgnoreCase("Error")) {
        	System.err.println("Bank registration to NameServer failed");
        	System.exit(1);
        } 
        
        // Now that we've registered with nameserver, start the bank's stuff
		while(true) {
			// Wait for bank to receive a new packet
			receiveData = new byte[1024];
			receivePacket = new DatagramPacket(receiveData,	receiveData.length);
			serverSocket.receive(receivePacket);
			
			InetAddress replyIPAddress = receivePacket.getAddress();
			int replyPort = receivePacket.getPort();
			
			// Send ACK
			sendData = "ACK".getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length, 
			        replyIPAddress, replyPort);
			serverSocket.send(sendPacket);
			
			// Convert reply to a String
			reply = new String(receivePacket.getData());
			reply = reply.trim();
			
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
	    		
	    		// Simulate packet loss
	    		x = Math.random();
	    		if (x < PACKET_LOSS_SIM) {
	    			serverSocket.send(sendPacket);
	    		}

	    		// Set timeout to defined amount of time
	    		serverSocket.setSoTimeout(TIMEOUT);
	    		receiveData = new byte[1024];
	    		receivePacket = new DatagramPacket(receiveData, 
	    				receiveData.length);

	    		// Wait for ACK
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
	    			break;
	    		}
	    		
	    		System.out.println(split[0] + " OK");
	    	} else {
	    		// Reply to connection that sale is not ok
	    		sendData = "1".getBytes();
	    		sendPacket = new DatagramPacket(sendData, sendData.length, 
	    				replyIPAddress, replyPort);
	    		
	    		// Simulate packet loss
	    		x = Math.random();
	    		if (x < PACKET_LOSS_SIM) {
	    			serverSocket.send(sendPacket);
	    		}

	    		// Set timeout to defined amount of time
	    		serverSocket.setSoTimeout(TIMEOUT);
	    		receiveData = new byte[1024];
	    		receivePacket = new DatagramPacket(receiveData, 
	    				receiveData.length);

	    		// Wait for ACK
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
	    			break;
	    		}
	    		
	    		System.out.println(split[0] + " NOT OK");
		    }
		}
		serverSocket.close();
	}
}
