package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class NameServer {
	
	// Define timeout period (in ms) and retry limit
	private static final int TIMEOUT = 5000;
	private static final int RETRIES = 3;
	// 0 = no packets arrive, 1 = all packets arrive
	private static final int PACKET_LOSS_SIM = 1;
			
	@SuppressWarnings("resource")
	public static void main(String args[]) throws Exception {
		
		HashMap<String, InetSocketAddress> nsMap = new HashMap<String, 
				InetSocketAddress>();
		int nsPort = 0;
		double x;
		// Set buffers
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		
		DatagramPacket sendPacket;
		
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
		
		// construct datagram socket with our given port
		DatagramSocket serverSocket = new DatagramSocket(nsPort);

		System.out.println("Name Server waiting for incoming connections "
        		+ "....");
		
		// Waiting for incoming messages
		while (true) {
			// receive message
			DatagramPacket receivePacket = new DatagramPacket(receiveData, 
					receiveData.length);
			serverSocket.receive(receivePacket);
			
			// We have a message at this point, decide what to do with it
			String data = new String(receivePacket.getData());
			data = data.trim();
			String msg[] = data.split(" ");
			
			System.out.println("DATA: " + data);
			for (int i = 0; i < msg.length; i++) {
				System.out.println(msg[i]);
			}
			
			// get the port of the client
			InetAddress IPAddress = receivePacket.getAddress();
			int cPort = receivePacket.getPort();
			
			// Send ACK
			sendData = "ACK".getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length, 
					IPAddress, cPort);
			serverSocket.send(sendPacket);
			
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
	    					IPAddress, cPort);
	    			
	    			// Simulate packet loss
	    			x = Math.random();
	    			if (x < PACKET_LOSS_SIM) {
	    				serverSocket.send(sendPacket);
	    			}
	    			
	    			// Set timeout to defined amount of time
	    			serverSocket.setSoTimeout(TIMEOUT);
	    			receivePacket = new DatagramPacket(receiveData, 
	    					receiveData.length);
	    			
	    			// Wait for ACK
	    			int attempts;
	    			for (attempts = 0; attempts < RETRIES; attempts++) {
		    			try {
		    				serverSocket.receive(receivePacket);
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
	    				System.err.println("0 No response, server offline.");
	    			}
	    			
	    			// Set timeout back to infinite
	    			serverSocket.setSoTimeout(0);
	    		}
	    		// Send success notice back to client
	    		sendData = "Success".getBytes();
    			sendPacket = new DatagramPacket(sendData, sendData.length, 
    					IPAddress, cPort);
    			
    			// Simulate packet loss
    			x = Math.random();
    			if (x < PACKET_LOSS_SIM) {
    				serverSocket.send(sendPacket);
    			}
    			
    			// Set timeout to defined amount of time
    			serverSocket.setSoTimeout(TIMEOUT);
    			receivePacket = new DatagramPacket(receiveData, 
    					receiveData.length);
    			
    			// Wait for ACK
    			int attempts;
    			for (attempts = 0; attempts < RETRIES; attempts++) {
	    			try {
	    				serverSocket.receive(receivePacket);
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
    				System.err.println("1 No response, server offline.");
    			}
		    	
	    		// We want to lookup here
	    	} else if(msg[0].equalsIgnoreCase("lookup")) {
	    		if (nsMap.containsKey(msg[1])) {
	    			// Send addr in the form "hostname:port"
	    			sendData = (nsMap.get(msg[1]).getHostName() + ":" + 
	    					nsMap.get(msg[1]).getPort()).getBytes();
	    			// Send the lookup result back to the client 
	    			sendPacket = new DatagramPacket(sendData, 
	    					sendData.length, IPAddress, cPort);
	    			
	    			// Simulate packet loss
	    			x = Math.random();
	    			if (x < PACKET_LOSS_SIM) {
	    				serverSocket.send(sendPacket);
	    			}
	    			
	    			// Set timeout to defined amount of time
	    			serverSocket.setSoTimeout(TIMEOUT);
	    			receivePacket = new DatagramPacket(receiveData, 
	    					receiveData.length);
	    			
	    			// Wait for ACK
	    			int attempts;
	    			for (attempts = 0; attempts < RETRIES; attempts++) {
		    			try {
		    				serverSocket.receive(receivePacket);
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
	    				System.err.println("2 No response, server offline.");
	    			}
	    		} else {
	    			// Send error notice back to client 
	    			sendData = ("Error: Process has not registered with the "
	    					+ "Name Server").getBytes();
	    			sendPacket = new DatagramPacket(sendData, 
	    					sendData.length, IPAddress, cPort);

	    			// Simulate packet loss
	    			x = Math.random();
	    			if (x < PACKET_LOSS_SIM) {
	    				serverSocket.send(sendPacket);
	    			}
	    			
	    			// Set timeout to defined amount of time
	    			serverSocket.setSoTimeout(TIMEOUT);
	    			receivePacket = new DatagramPacket(receiveData, 
	    					receiveData.length);
	    			
	    			// Wait for ACK
	    			int attempts;
	    			for (attempts = 0; attempts < RETRIES; attempts++) {
		    			try {
		    				serverSocket.receive(receivePacket);
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
	    				System.err.println("3 No response, server offline.");
	    			}
	    		}
	    	}
	    	
		}
	}
}
