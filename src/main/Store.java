package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Store {

	public static void main(String args[]) throws Exception {
		int storePort = 0;
		String stockFileName = null;
		int nsPort = 0;
		InetSocketAddress bank = null;
		InetSocketAddress content = null;
		TreeMap<Long, Float> stockData;
		DatagramPacket receivePacket;
		String reply;
		String[] replySplit;
		// Set buffers
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		
		// If there are more than 3 arguments
		if (args.length != 3) {
			System.err.println("Invalid command line arguments for Store");
			System.exit(1);
		}
		
		// Parse args
		try {
			storePort = Integer.parseInt(args[0]);
			stockFileName = args[1];
			nsPort = Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			System.err.println("Invalid command line arguments for Store");
			System.exit(1);
		}
		
		// Construct datagram socket with our given port for store
		DatagramSocket serverSocket = new DatagramSocket(storePort);
		
		// Register with name server
		DatagramSocket clientSocket = new DatagramSocket();
		// Set server's IP address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// Create data to be sent
		sendData = ("register " + "Content " + "127.0.0.1" + " " + storePort)
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
        	System.err.println("Registration with NameServer failed");
        	System.exit(1);
        } 
		
		// Request IP addr and port of Bank
		sendData = "lookup Bank".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 
				nsPort);
		
		// Wait for a reply from nameserver
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		// Convert reply to a String
		reply = new String(receivePacket.getData());
		replySplit = reply.split(" ");
		
		if (replySplit[0].equalsIgnoreCase("Error")) {
			System.err.println("Bank has not registered");
		} else {
			try {
				int tempPort = Integer.parseInt(replySplit[1]);
				bank = new InetSocketAddress(replySplit[0], tempPort);
			} catch (Exception e) {
				System.err.println("Bank has not registered");
			}
		}
		
		// Request IP addr and port of Content
		sendData = "lookup Content".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 
				nsPort);
		
		// Wait for a reply from nameserver
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		// Convert reply to a String
		reply = new String(receivePacket.getData());
		replySplit = reply.split(" ");
		
		if (replySplit[0].equalsIgnoreCase("Error")) {
			System.err.println("Content has not registered");
		} else {
			try {
				int tempPort = Integer.parseInt(replySplit[1]);
				content = new InetSocketAddress(replySplit[0], tempPort);
			} catch (Exception e) {
				System.err.println("Content has not registered");
			}
		}
		
		// At this point, we want to read in the stock-file
        stockData = new TreeMap<Long, Float>();
        
        try (BufferedReader br = new BufferedReader(
        		new FileReader(stockFileName))) {
        	String line = br.readLine();
        	while (line != null) {
	    
        		String[] split = line.split(" ");
	
        		Long itemId = Long.parseLong(split[0]);
        		Float itemPrice = Float.parseFloat(split[1]);
        		stockData.put(itemId, itemPrice);
	    
        		line = br.readLine();
        	}  
        } catch (IOException e) {
        	System.err.println("stock-file does not exist");
        }
		
        
        // Wait for Client to send a request
        while(true) {
        	// Receive request for item-data
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			
			// Get IPAddress and Port to reply to
			InetAddress replyIPAddress = receivePacket.getAddress();
			int replyPort = receivePacket.getPort();
			
			// Convert request to a String
			reply = new String(receivePacket.getData());
			replySplit = reply.split(" ");
        	
			long itemID = 0l;
	    	
	    	// Check what request has been sent
	    	if (replySplit.length == 1) {
	    		// Request to list items
	    		// Send back length to expect first
	    		sendData = Integer.toString(stockData.entrySet().size())
	    				.getBytes();
	    		for (Entry<Long, Float> entry : stockData.entrySet()) {
	    			sendData = (entry.getKey() + " " + entry.getValue())
	    					.getBytes();
	    			sendPacket = new DatagramPacket(sendData, sendData.length, 
							replyIPAddress, replyPort);
					serverSocket.send(sendPacket);
	    		}
	    	} else {
	    		// Request to buy item. replySplit[] = [itemID, ccNo]
	    		itemID = 3200720100l + Long.parseLong(replySplit[0]);
	    		
	    		sendData = (itemID + " " + stockData.get(itemID) + " " + 
	    				replySplit[1]).getBytes();
	    		
	    		// Send request to bank
	    		sendPacket = new DatagramPacket(sendData, sendData.length, 
	    				bank.getAddress(), bank.getPort());
				serverSocket.send(sendPacket);
				
				// Wait for response from bank
				receivePacket = new DatagramPacket(receiveData, 
						receiveData.length);
				serverSocket.receive(receivePacket);
				
				// Convert request to a String
				reply = new String(receivePacket.getData());
				replySplit = reply.split(" ");
				
				// DO NOT MAKE THE SAME STUPID MISTAKE AS ASSIGNMENT 1
				// BANK WILL SEND BACK A 1 OR 0, NOT "OK" OR "NOT OK" YOU MORON
				
		        if (replySplit[1].equalsIgnoreCase("1")) {
		        	// Send "transaction aborted" string to client
		        	sendData = (itemID + "transaction aborted").getBytes();
		        	sendPacket = new DatagramPacket(sendData, sendData.length, 
							replyIPAddress, replyPort);
					serverSocket.send(sendPacket);
		        } else {
		        	// Contact Content and retrieve content
		        	sendData = ("" + itemID).getBytes();
		        	sendPacket = new DatagramPacket(sendData, sendData.length, 
		    				content.getAddress(), content.getPort());
					serverSocket.send(sendPacket);
					
		        	// Wait for a reply from content
					receivePacket = new DatagramPacket(receiveData, 
							receiveData.length);
					serverSocket.receive(receivePacket);
					
					// Convert request to a String
					reply = new String(receivePacket.getData());
					replySplit = reply.split(" ");
			        
			        if (replySplit.length > 1) {
			        	// Failed
			        	sendData = (itemID + "transaction aborted").getBytes();
			        	sendPacket = new DatagramPacket(sendData, 
			        			sendData.length, replyIPAddress, replyPort);
						serverSocket.send(sendPacket);
			        } else {
			        	// Give content to client
			        	sendData = (itemID + " " + stockData.get(itemID) + " " + 
			        	replySplit[0]).getBytes();
			        	sendPacket = new DatagramPacket(sendData, 
			        			sendData.length, replyIPAddress, replyPort);
						serverSocket.send(sendPacket);
			        }
		        }
		        			
	    	}
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        }
	}

}
