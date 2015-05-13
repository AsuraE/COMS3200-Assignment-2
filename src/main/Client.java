package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Client {

	public static void main(String args[]) throws Exception {
		
		Integer nsPort = 0;
		Integer request = 0;
		InetSocketAddress store = null;
		String ccNo = "1234567890123456";
		
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
		clientSocket.send(sendPacket);
		
		// receive reply message from server
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		
		// Convert reply to a string
		reply = new String(receivePacket.getData());
		
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
        	}
        }
		
		// At this point, we have Store's info, so let's send it our request		
		if (request == 0) {
        	sendData = request.toString().getBytes();
        	sendPacket = new DatagramPacket(sendData, sendData.length,
        			store.getAddress(), store.getPort());
    		clientSocket.send(sendPacket);
        } else {
        	sendData = (request + " " + ccNo).getBytes();
        	sendPacket = new DatagramPacket(sendData, sendData.length,
        			store.getAddress(), store.getPort());
    		clientSocket.send(sendPacket);
        }
		
        // Wait for Store's reply
        if (request == 0) {
        	
        	receivePacket = new DatagramPacket(receiveData, receiveData.length);
    		clientSocket.receive(receivePacket);
    		
    		// Convert reply to a string and print it
    		reply = new String(receivePacket.getData());
           	System.out.println(reply);
           	
        } else {
        	
        	// Print out success in format item-id ($ item-price) CONTENT item-content
        	receivePacket = new DatagramPacket(receiveData, receiveData.length);
    		clientSocket.receive(receivePacket);
    		
    		// Convert reply to a string 
    		reply = new String(receivePacket.getData());
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
