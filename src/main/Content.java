package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;

public class Content {

	public static void main(String args[]) throws Exception {
		
		int contentPort = 0;
		String contentFileName = null;
		int nsPort = 0;
		HashMap<Long, String> contentData;
		DatagramPacket receivePacket;
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
		DatagramSocket serverSocket = new DatagramSocket(contentPort);
		
		// Set buffers
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		System.out.println("Content waiting for incoming connections "
        		+ "....");
		
		// Register with name server
		DatagramSocket clientSocket = new DatagramSocket();
		// Set server's IP address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// Create data to be sent
		sendData = ("register " + "Content " + "127.0.0.1" + " " + contentPort)
				.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, 
				sendData.length, IPAddress, nsPort);
		clientSocket.send(sendPacket);
		// Request sent at this point, registered with nameserver
		
		// Wait for a reply from nameserver
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		// Convert reply to a String
		reply = new String(receivePacket.getData());
		
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
			
			// Convert request to a String
			reply = new String(receivePacket.getData());
			
			// At this point we have the request in the form "item-id"
			// Respond with either item-data or an abort if no item-data found
			if (contentData.containsKey(reply)) {
				sendData = (contentData.get(reply)).getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, 
						replyIPAddress, replyPort);
				serverSocket.send(sendPacket);
			} else {
				sendData = (reply + " transaction aborted").getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, 
						replyIPAddress, replyPort);
				serverSocket.send(sendPacket);
			}
			
		}
	}

}
