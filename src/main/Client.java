package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class Client {
	
	// Define timeout period (in ms) and retry limit
	private static final int TIMEOUT = 5000;
	private static final int RETRIES = 3;
	// 0 = no packets arrive, 1 = all packets arrive
	private static final int PACKET_LOSS_SIM = 1;
	
	public static void main(String args[]) throws Exception {
		
		Integer nsPort = 0;
		Integer request = 0;
		InetSocketAddress store = null;
		String ccNo = "1234567890123456";
		double x;
		int attempts;
		
		// construct datagram socket
		DatagramSocket clientSocket = new DatagramSocket();
		// set server's ip address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// set buffers
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		DatagramPacket sendPacket;
		DatagramPacket receivePacket;
		String reply;
		
		// If there are not 2 arguments throw an error
		if (args.length != 2) {
			System.err.println("Invalid command line arguments");
			System.exit(1);
		}
		
		// Parse args
		try {
			request = Integer.parseInt(args[0]);
			nsPort = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid command line arguments");
		}
			
		// Contact NameServer for Store's IP and port	
		sendData = "lookup Store".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 
				nsPort);

		// Simulate packet loss
		x = Math.random();
		if (x < PACKET_LOSS_SIM) {
			clientSocket.send(sendPacket);
		}

		// Set timeout to defined amount of time
		clientSocket.setSoTimeout(TIMEOUT);
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, 
				receiveData.length);

		// Wait for ACK
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
			System.err.println("Client unable to connect with NameServer");
			System.exit(1);
		}

		// receive reply message from server
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		// Send ACK
		sendData = "ACK".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, 
		        IPAddress, nsPort);
		clientSocket.send(sendPacket);
		
		// Convert reply to a string
		reply = new String(receivePacket.getData());
		reply = reply.trim();
		
		// Check for errors
		String[] replySplit = reply.split(":");
		
		if (replySplit[0].equalsIgnoreCase("Error")) {
        	System.err.println("Client unable to connect with Store");
        	System.exit(1);
        } else {
        	try {
        		int tempPort = Integer.parseInt(replySplit[1]);
        		store = new InetSocketAddress(replySplit[0], tempPort);
        	} catch (Exception e) {
        		System.err.println("Client unable to connect with Store");
        		System.exit(1);
        	}
        }
		
		// At this point, we have Store's info, so let's send it our request		
		if (request == 0) {
        	sendData = request.toString().getBytes();
        	sendPacket = new DatagramPacket(sendData, sendData.length,
        			store.getAddress(), store.getPort());
        	
        	// Simulate packet loss
    		x = Math.random();
    		if (x < PACKET_LOSS_SIM) {
    			clientSocket.send(sendPacket);
    		}

    		// Set timeout to defined amount of time
    		clientSocket.setSoTimeout(TIMEOUT);
    		receiveData = new byte[1024];
    		receivePacket = new DatagramPacket(receiveData, 
    				receiveData.length);

    		// Wait for ACK
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
    			System.err.println("Client unable to connect with Store");
    			System.exit(1);
    		}
        } else {
        	sendData = (request + " " + ccNo).getBytes();
        	sendPacket = new DatagramPacket(sendData, sendData.length,
        			store.getAddress(), store.getPort());
        	
        	// Simulate packet loss
    		x = Math.random();
    		if (x < PACKET_LOSS_SIM) {
    			clientSocket.send(sendPacket);
    		}

    		// Set timeout to defined amount of time
    		clientSocket.setSoTimeout(TIMEOUT);
    		receiveData = new byte[1024];
    		receivePacket = new DatagramPacket(receiveData, 
    				receiveData.length);

    		// Wait for ACK
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
    			System.err.println("Client unable to connect with Store");
    			System.exit(1);
    		}
        }
		
        // Wait for Store's reply
        if (request == 0) {
        	receiveData = new byte[1024];
        	receivePacket = new DatagramPacket(receiveData, receiveData.length);
    		clientSocket.receive(receivePacket);
    		
    		// Send ACK
    		sendData = "ACK".getBytes();
    		sendPacket = new DatagramPacket(sendData, sendData.length, 
    				store.getAddress(), store.getPort());
    		clientSocket.send(sendPacket);
    		
    		// Wait for store to send the expected number of items
    		reply = new String(receivePacket.getData());
    		reply = reply.trim();
    		int entries = 0;
    		
    		// Parse as int
    		try {
    			System.out.println("Reply: " + reply);
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
        		sendData = "ACK".getBytes();
        		sendPacket = new DatagramPacket(sendData, sendData.length, 
        				store.getAddress(), store.getPort());
        		clientSocket.send(sendPacket);
        		
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
    		sendData = "ACK".getBytes();
    		sendPacket = new DatagramPacket(sendData, sendData.length, 
    				store.getAddress(), store.getPort());
    		clientSocket.send(sendPacket);
    		
    		// Convert reply to a string 
    		reply = new String(receivePacket.getData());
    		reply = reply.trim();
	    	replySplit = reply.split(" ");
	    	
	    	if (replySplit[2].equalsIgnoreCase("aborted")) {
	    		System.out.println(replySplit[0] + "transaction aborted");
	    	} else {
	    		System.out.println(replySplit[0] + " ($ " + replySplit[1] + 
	    				") CONTENT " + replySplit[2]);
	    	}
        	
        }
        
        clientSocket.close();
	}
}
