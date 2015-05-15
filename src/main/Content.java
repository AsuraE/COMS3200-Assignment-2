package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class Content {
	
	// Define timeout period (in ms) and retry limit
	private static final int TIMEOUT = 5000;
	private static final int RETRIES = 3;
	// 0 = no packets arrive, 1 = all packets arrive
	private static final int PACKET_LOSS_SIM = 1;
	
	public static void main(String args[]) throws Exception {
		
		int contentPort = 0;
		String contentFileName = null;
		int nsPort = 0;
		HashMap<Long, String> contentData;
		DatagramPacket receivePacket;
		String reply;
		double x;
		int attempts;
		
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
		DatagramSocket serverSocket = new DatagramSocket(contentPort);
		
		// Set buffers
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		System.out.println("Content waiting for incoming connections "
        		+ "....");
		
		// Set server's IP address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// Create data to be sent
		sendData = ("register " + "Content " + "127.0.0.1" + " " + contentPort)
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
		receivePacket = new DatagramPacket(receiveData, 
				receiveData.length);

		// Wait for ACK
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
			System.err.println("Content unable to connect with NameServer");
			System.exit(1);
		}
		
		// Request sent at this point, registered with nameserver
		
		// Wait for a reply from nameserver
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
		
		if (reply.equalsIgnoreCase("Error")) {
        	System.err.println("Content registration with NameServer failed");
        	System.exit(1);
        } 
					
		// At this point, we want to read in the content-file
        contentData = new HashMap<Long, String>();		
				
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
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			
			// Get IPAddress and Port to reply to
			InetAddress replyIPAddress = receivePacket.getAddress();
			int replyPort = receivePacket.getPort();
			
			// Send ACK
			sendData = "ACK".getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length, 
			        replyIPAddress, replyPort);
			serverSocket.send(sendPacket);
			
			// Convert request to a String
			reply = new String(receivePacket.getData());
			reply = reply.trim();
			
			// At this point we have the request in the form "item-id"
			// Respond with either item-data or an abort if no item-data found
			if (contentData.containsKey(reply)) {
				sendData = (contentData.get(reply)).getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, 
						replyIPAddress, replyPort);
				
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
					System.err.println("No response, server offline.");
					break;
				}
			} else {
				sendData = (reply + " transaction aborted").getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, 
						replyIPAddress, replyPort);
				
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
					System.err.println("No response, server offline.");
					break;
				}
			}
		}
		serverSocket.close();
	}
}
