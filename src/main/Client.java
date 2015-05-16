package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class Client {
	
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
	private static DatagramSocket clientSocket; 
	// Datagram packet to receive
	private static DatagramPacket receivePacket;
	// Datagram packet to send
	private static DatagramPacket sendPacket;
	
	public static void main(String args[]) throws Exception {
		
		// Port number for NameServer
		Integer nsPort = 0;
		// Int representing request type
		Integer request = 0;
		// Address for Store
		InetSocketAddress store = null;
		// Our super duper secret credit card number
		String ccNo = "1234567890123456";
		// construct datagram socket
		clientSocket = new DatagramSocket();
		// set server's ip address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// String to store server replies
		String reply;
		
		// If there are not 2 arguments throw an error
		if (args.length != 2) {
			System.err.println("Invalid command line arguments");
			clientSocket.close();
			System.exit(1);
		}
		
		// Parse args
		try {
			request = Integer.parseInt(args[0]);
			nsPort = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid command line arguments");
			clientSocket.close();
			System.exit(1);
		}
			
		// Contact NameServer for Store's IP and port	
		sendData = "lookup Store".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 
				nsPort);

		// Send message while simulating packet loss
		// If our send is unsuccessful, end the process
    	if (!simulatePacketLoss()) {
    		clientSocket.close();
    		System.exit(1);
		}

		// receive reply message from server
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		// Send ACK
		ack(IPAddress, nsPort);
		
		// Convert reply to a string
		reply = new String(receivePacket.getData());
		reply = reply.trim();
		
		// Check for errors
		String[] replySplit = reply.split(" ");
		
		if (replySplit[0].equalsIgnoreCase("Error")) {
        	System.err.println("Client unable to connect with Store");
        	clientSocket.close();
        	System.exit(1);
        } else {
        	try {
        		int tempPort = Integer.parseInt(replySplit[1]);
        		store = new InetSocketAddress(replySplit[0], tempPort);
        	} catch (Exception e) {
        		System.err.println("Client unable to connect with Store");
        		clientSocket.close();
        		System.exit(1);
        	}
        }
		
		// At this point, we have Store's info, so let's send it our request		
		if (request == 0) {
        	sendData = request.toString().getBytes();
        	sendPacket = new DatagramPacket(sendData, sendData.length,
        			store.getAddress(), store.getPort());
        	
        	// Send message while simulating packet loss
    		// If our send is unsuccessful, end the process
        	if (!simulatePacketLoss()) {
        		clientSocket.close();
    			System.exit(1);
    		}
        } else {
        	sendData = (request + " " + ccNo).getBytes();
        	sendPacket = new DatagramPacket(sendData, sendData.length,
        			store.getAddress(), store.getPort());
        	
        	// Send message while simulating packet loss
    		// If our send is unsuccessful, end the process
        	if (!simulatePacketLoss()) {
        		clientSocket.close();
    			System.exit(1);
    		}
        }
		
        // Wait for Store's reply
        if (request == 0) {
        	receiveData = new byte[1024];
        	receivePacket = new DatagramPacket(receiveData, receiveData.length);
    		clientSocket.receive(receivePacket);
    		
    		// Send ACK
    		ack(store.getAddress(), store.getPort());
    		
    		// Wait for store to send the expected number of items
    		reply = new String(receivePacket.getData());
    		reply = reply.trim();
    		int entries = 0;
    		
    		// Parse as int
    		try {
    			entries = Integer.parseInt(reply);
    		} catch (NumberFormatException e) {
    			System.err.println("Ya dun goof'd");
    		}
    		
    		// Wait for replies containing each of the expected items
    		for (int i = 0; i < entries; i++) {
    			receiveData = new byte[1024];
    			receivePacket = new DatagramPacket(receiveData, 
    					receiveData.length);
        		clientSocket.receive(receivePacket);
        		
        		// Send ACK
        		ack(store.getAddress(), store.getPort());
        		
        		// Convert to string and print
        		reply = new String(receivePacket.getData());
        		reply = reply.trim();
    			System.out.println(reply);
    		}
        } else {
        	
        	// Print out success in format item-id ($ item-price) CONTENT item-content
        	receiveData = new byte[1024];
        	receivePacket = new DatagramPacket(receiveData, receiveData.length);
    		clientSocket.receive(receivePacket);
    		
    		// Send ACK
    		ack(store.getAddress(), store.getPort());
    		
    		// Convert reply to a string 
    		reply = new String(receivePacket.getData());
    		reply = reply.trim();
    		// Split reply on whitespace
	    	replySplit = reply.split(" ");
	    	
	    	if (replySplit[2].equalsIgnoreCase("aborted")) {
	    		System.out.println(replySplit[0] + " transaction aborted");
	    	} else {
	    		System.out.println(replySplit[0] + " ($ " + replySplit[1] + 
	    				") CONTENT " + replySplit[2]);
	    	}
        	
        }
        clientSocket.close();
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
			clientSocket.send(sendPacket);
		}
		
		// Set timeout to defined amount of time
		clientSocket.setSoTimeout(TIMEOUT);
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		// Wait for ACK
		int attempts;
		for (attempts = 0; attempts < RETRIES; attempts++) {
			try {
				clientSocket.receive(receivePacket);
				break;
			} catch (SocketTimeoutException se) {
				// ACK not received, resend packet and again,
				// simulate packet loss
				x = Math.random();
    			if (x < PACKET_LOSS_SIM) {
    				clientSocket.send(sendPacket);
    			}
			}
		}
		
		// Set timeout back to infinite
		clientSocket.setSoTimeout(0);
		
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
		clientSocket.send(sendPacket);
	}
}
