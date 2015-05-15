package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class NameServer {
	
	// Define timeout period (in ms) 
	private static final int TIMEOUT = 10000;
	// Define amount of times to retry
	private static final int RETRIES = 5;
	// 0 = no packets arrive, 1 = all packets arrive
	private static final double PACKET_LOSS_SIM = 0.75;
	// Send and receive buffers
	private static	byte[] receiveData = new byte[1024];
	private static byte[] sendData = new byte[1024];
	// Datagram socket 
	private static DatagramSocket serverSocket; 
	// Datagram packet to receive
	private static DatagramPacket receivePacket;
	// Datagram packet to send
	private static DatagramPacket sendPacket;
	
	public static void main(String args[]) throws Exception {
		
		// Map of processes to their ports
		HashMap<String, InetSocketAddress> nsMap = new HashMap<String, 
				InetSocketAddress>();
		
		// Port number for Nameserver
		int nsPort = 0;
		
		// Error out if more than 1 command line is given
		if (args.length > 1) {
			System.err.println("Invalid command line arguments for NameServer");
			System.exit(1);
		}
		
		// Try parsing port number
		try {
			nsPort = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid command line arguments for NameServer");
			System.exit(1);
		}
		
		// Create new datagram socket
		serverSocket = new DatagramSocket(nsPort);

		System.out.println("Name Server waiting for incoming connections...");
		
		// Waiting for incoming messages
		while (true) {
			// Define new datagram receive packet and receive message
			receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, 
					receiveData.length);
			serverSocket.receive(receivePacket);
			
			// We have a message at this point, decide what to do with it
			String data = new String(receivePacket.getData());
			data = data.trim();
			String msg[] = data.split(" ");
			
			// Get the port of the client to reply to
			InetAddress replyIPAddress = receivePacket.getAddress();
			int replyPort = receivePacket.getPort();
			
			// Send ACK
			ack(replyIPAddress, replyPort);
			
			// We want to register a process to name server here
	    	if(msg[0].equalsIgnoreCase("register")) {

	    		try {
		    		// Create an inet socket address
		    		InetSocketAddress addr = new InetSocketAddress(msg[2], 
		    				Integer.parseInt(msg[3]));
		    		// Map this to the server name
		    		nsMap.put(msg[1], addr);
	    		} catch (Exception e) {
	    			System.out.println(e);
	    			// Send error notice back to client 
	    			sendData = "Error".getBytes();
	    			sendPacket = new DatagramPacket(sendData, sendData.length, 
	    					replyIPAddress, replyPort);
	    			// Send message while simulating packet loss
	    			simulatePacketLoss();
	    		}
	    		// Send success notice back to client
	    		sendData = "Success".getBytes();
    			sendPacket = new DatagramPacket(sendData, sendData.length, 
    					replyIPAddress, replyPort);
    			// Send message while simulating packet loss
    			simulatePacketLoss();
		    	
	    		// We want to lookup here
	    	} else if(msg[0].equalsIgnoreCase("lookup")) {
	    		if (nsMap.containsKey(msg[1])) {
	    			// Send addr in the form "hostname:port"
	    			sendData = (nsMap.get(msg[1]).getHostName() + " " + 
	    					nsMap.get(msg[1]).getPort()).getBytes();
	    			// Send the lookup result back to the client 
	    			sendPacket = new DatagramPacket(sendData, 
	    					sendData.length, replyIPAddress, replyPort);
	    			// Send message while simulating packet loss
	    			simulatePacketLoss();
	    		} else {
	    			// Send error notice back to client 
	    			sendData = ("Error: Process has not registered with the "
	    					+ "Name Server").getBytes();
	    			sendPacket = new DatagramPacket(sendData, 
	    					sendData.length, replyIPAddress, replyPort);
	    			// Send message while simulating packet loss
	    			simulatePacketLoss();
	    			
	    		}
	    	}
		}
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
