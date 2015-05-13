package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class NameServer {
	
	public static void main(String args[]) throws Exception {
		
		HashMap<String, InetSocketAddress> nsMap = new HashMap<String, 
				InetSocketAddress>();
		
		// Try parsing port number
		int nsPort = 0;
		
		try {
			nsPort = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Invalid command line arguments for NameServer");
			System.exit(1);
		}
		
		// construct datagram socket with our given port
		DatagramSocket serverSocket = new DatagramSocket(nsPort);
		
		// set buffers
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		System.out.println("Name Server waiting for incoming connections "
        		+ "....");
		
		// waiting for incoming messages
		while (true) {
			
			// receive message from client
			DatagramPacket receivePacket = new DatagramPacket(receiveData, 
					receiveData.length);
			serverSocket.receive(receivePacket);
			
			// We have a message at this point, decide what to do with it
			String data = new String(receivePacket.getData());
			String msg[] = data.split(" ");
			
			// get the port of the client
			InetAddress IPAddress = receivePacket.getAddress();
			int cPort = receivePacket.getPort();
			
			// We want to register a process to name server here
	    	if(msg[0].equalsIgnoreCase("register")) {
	    		
	    		try {
		    		// Create an inet socket address
		    		InetSocketAddress addr = new InetSocketAddress(msg[2], 
		    				Integer.parseInt(msg[3]));
		    		// Map this to the server name
		    		nsMap.put(msg[1], addr);
	    		} catch (Exception e) {
	    			// Send error notice back to client 
	    			sendData = "Error".getBytes();
	    			DatagramPacket sendPacket = new DatagramPacket(sendData, 
	    					sendData.length, IPAddress, cPort);
	    			serverSocket.send(sendPacket);
	    		    break;
	    		}
	    		// Send success notice back to client
	    		sendData = "Success".getBytes();
    			DatagramPacket sendPacket = new DatagramPacket(sendData, 
    					sendData.length, IPAddress, cPort);
    			serverSocket.send(sendPacket);
		    	
	    		// We want to lookup here
	    	} else if(msg[0].equalsIgnoreCase("lookup")) {
	    		if (nsMap.containsKey(msg[1])) {
	    			// Send addr in the form "hostname:port"
	    			sendData = (nsMap.get(msg[1]).getHostName() + ":" + 
	    					nsMap.get(msg[1]).getPort()).getBytes();
	    			// Send the lookup result back to the client 
	    			DatagramPacket sendPacket = new DatagramPacket(sendData, 
	    					sendData.length, IPAddress, cPort);
	    			serverSocket.send(sendPacket);
	    		} else {
	    			// Send error notice back to client 
	    			sendData = ("Error: Process has not registered with the "
	    					+ "Name Server").getBytes();
	    			DatagramPacket sendPacket = new DatagramPacket(sendData, 
	    					sendData.length, IPAddress, cPort);
	    			serverSocket.send(sendPacket);
	    		}
	    	}
	    	
		}
		serverSocket.close();
	}
}
