package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class Content {
	
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
		
		// Port number for Content
		int contentPort = 0;
		// Port number for NameServer
		int nsPort = 0;
		// File name for content file
		String contentFileName = null;
		// Map to store content to item-id
		HashMap<Long, String> contentData;
		// String to store server replies
		String reply;
		
		// If there are more than 3 arguments
		if (args.length != 3) {
			System.err.println("Invalid command line arguments for Content");
			System.exit(1);
		}
		
		// Parse contentPort
		try {
			contentPort = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.err.println("Content unable to listen on given port");
			System.exit(1);
		}
		
		// Parse nsPort
		try {
			nsPort = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.err.println("Content registration to NameServer failed");
			System.exit(1);
		}
		
		// Parse content file name
		contentFileName = args[1];
		
		// Construct datagram socket with our given port for content
		serverSocket = new DatagramSocket(contentPort);
		
		System.out.println("Content waiting for incoming connections...");
		
		// Set server's IP address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		
		// Create data to be sent
		sendData = ("register " + "Content " + "127.0.0.1" + " " + contentPort)
				.getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 
				nsPort);
		
		// Send message while simulating packet loss
		// If our send is unsuccessful, end the process
		if (!simulatePacketLoss()) {
			serverSocket.close();
			System.exit(1);
		}
		
		// Wait for a reply from nameserver
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);
		
		// Send ACK
		ack(IPAddress, nsPort);
		
		// Convert reply to a String
		reply = new String(receivePacket.getData());
		reply = reply.trim();
		
		// End process if registration fails
		if (reply.equalsIgnoreCase("Error")) {
        	System.err.println("Content registration with NameServer failed");
        	serverSocket.close();
        	System.exit(1);
        } 
					
		// Create a new map to hold the content-file
        contentData = new HashMap<Long, String>();		
		
        // Read in the data from the content file
        try (BufferedReader br = new BufferedReader(
        		new FileReader(contentFileName))) {
        	String line = br.readLine();
        	while (line != null) {
	    
        		String[] split = line.split(" ");
	
        		Long itemId = Long.parseLong(split[0]);
        		String itemContent = split[1];
        		contentData.put(itemId, itemContent);
	    
        		line = br.readLine();
        	}  
        }
        
        // Now Content waits for a request for item data
		while(true) {
			// Receive request for item-data
			receiveData = new byte[1024];
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			
			// Get IPAddress and Port to reply to
			InetAddress replyIPAddress = receivePacket.getAddress();
			int replyPort = receivePacket.getPort();
			
			// Send ACK
			ack(replyIPAddress, replyPort);
			
			// Convert request to a String
			reply = new String(receivePacket.getData());
			reply = reply.trim();
			Long key = 0l;
			
			try {
				key = Long.parseLong(reply);
			} catch (Exception e) {
				System.err.println("Invalid key");
			}

			// At this point we have the request in the form "item-id"
			// Respond with either item-data or an abort if no item-data found
			if (contentData.containsKey(key)) {
				sendData = (contentData.get(key)).getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, 
						replyIPAddress, replyPort);
				
				// Send message while simulating packet loss
				// If our send is unsuccessful, break from the loop
				if (!simulatePacketLoss()) {
					break;
				}
			} else {
				sendData = (reply + " transaction aborted").getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, 
						replyIPAddress, replyPort);
				
				// Send message while simulating packet loss
				// If our send is unsuccessful, break from the loop
				if (!simulatePacketLoss()) {
					break;
				}
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
