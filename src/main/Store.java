package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Store {
	
	// Define timeout period (in ms) and retry limit
	private static final int TIMEOUT = 5000;
	private static final int RETRIES = 3;
	// 0 = no packets arrive, 1 = all packets arrive
	private static final int PACKET_LOSS_SIM = 1;
	
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
		double x;
		int attempts;
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

		// Set server's IP address
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
		// Create data to be sent
		sendData = ("register " + "Store " + "127.0.0.1" + " " + storePort)
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
		receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, 
				receiveData.length);

		// Wait for ACK
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
			System.err.println("Store unable to connect with NameServer");
			System.exit(1);
		}
		// Request sent at this point, registered with nameserver
		
		// Wait for a reply from nameserver
		receiveData = new byte[1024];
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
        	System.err.println("Registration with NameServer failed");
        	System.exit(1);
        } 

		// Request IP addr and port of Bank
		sendData = "lookup Bank".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 
				nsPort);
		
		// Simulate packet loss
		x = Math.random();
		if (x < 0.5) {
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
		        break;
			} catch (SocketTimeoutException se) {
				// ACK not received, resend packet and again,
				// simulate packet loss
				x = Math.random();
				if (x < 0.5) {
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
			System.exit(1);
		}
		
		// Wait for a reply from nameserver
		receiveData = new byte[1024];
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
		replySplit = reply.split(" ");
		
		if (replySplit[0].equalsIgnoreCase("Error")) {
			System.err.println("Bank has not registered");
		} else {
			try {
				int tempPort = Integer.parseInt(replySplit[1]);
				bank = new InetSocketAddress(replySplit[0], tempPort);
			} catch (Exception e) {
				System.err.println("2 Bank has not registered");
			}
		}

		// Request IP addr and port of Content
		sendData = "lookup Content".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 
				nsPort);
		// Simulate packet loss
		x = Math.random();
		if (x < 0.5) {
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
		        break;
			} catch (SocketTimeoutException se) {
				// ACK not received, resend packet and again,
				// simulate packet loss
				x = Math.random();
				if (x < 0.5) {
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
			System.exit(1);
		}
		// Wait for a reply from nameserver
		receiveData = new byte[1024];
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
		
        
        System.out.println("Store waiting for incoming connections");
        // Wait for Client to send a request
        while(true) {
        	// Receive request for item-data
        	receiveData = new byte[1024];
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
			replySplit = reply.split(" ");
        	
			long itemID = 0l;
	    	
	    	// Check what request has been sent
	    	if (replySplit.length == 1) {
	    		// Request to list items
	    		// Send back length to expect first
	    		sendData = Integer.toString(stockData.entrySet().size())
	    				.getBytes();
	    		
	    		sendPacket = new DatagramPacket(sendData, sendData.length, 
						replyIPAddress, replyPort);
    			
    			// Simulate packet loss
    			x = Math.random();
    			if (x < PACKET_LOSS_SIM) {
    				serverSocket.send(sendPacket);
    			}

    			// Set timeout to defined amount of time
    			serverSocket.setSoTimeout(TIMEOUT);
    			receiveData = new byte[1024];
    			receivePacket = new DatagramPacket(receiveData, 
    					receiveData.length);

    			// Wait for ACK
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
    				break;
    			}
	    		
	    		for (Entry<Long, Float> entry : stockData.entrySet()) {
	    			sendData = (entry.getKey() + " " + entry.getValue())
	    					.getBytes();
	    			sendPacket = new DatagramPacket(sendData, sendData.length, 
							replyIPAddress, replyPort);
	    			
	    			// Simulate packet loss
	    			x = Math.random();
	    			if (x < PACKET_LOSS_SIM) {
	    				serverSocket.send(sendPacket);
	    			}

	    			// Set timeout to defined amount of time
	    			serverSocket.setSoTimeout(TIMEOUT);
	    			receiveData = new byte[1024];
	    			receivePacket = new DatagramPacket(receiveData, 
	    					receiveData.length);

	    			// Wait for ACK
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
	    				break;
	    			}
	    		}
	    	} else {
	    		// Request to buy item. replySplit[] = [itemID, ccNo]
	    		itemID = 3200720100l + Long.parseLong(replySplit[0]);
	    		
	    		sendData = (itemID + " " + stockData.get(itemID) + " " + 
	    				replySplit[1]).getBytes();
	    		
	    		// Send request to bank
	    		sendPacket = new DatagramPacket(sendData, sendData.length, 
	    				bank.getAddress(), bank.getPort());
	    		
	    		// Simulate packet loss
	    		x = Math.random();
	    		if (x < PACKET_LOSS_SIM) {
	    			serverSocket.send(sendPacket);
	    		}

	    		// Set timeout to defined amount of time
	    		serverSocket.setSoTimeout(TIMEOUT);
	    		receiveData = new byte[1024];
	    		receivePacket = new DatagramPacket(receiveData, 
	    				receiveData.length);

	    		// Wait for ACK
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
	    			break;
	    		}
				
				// Wait for response from bank
	    		receiveData = new byte[1024];
				receivePacket = new DatagramPacket(receiveData, 
						receiveData.length);
				serverSocket.receive(receivePacket);
				
				// Send ACK
				sendData = "ACK".getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, 
				        bank.getAddress(), bank.getPort());
				serverSocket.send(sendPacket);
				
				// Convert request to a String
				reply = new String(receivePacket.getData());
				reply = reply.trim();
				
				// DO NOT MAKE THE SAME STUPID MISTAKE AS ASSIGNMENT 1
				// BANK WILL SEND BACK A 1 OR 0, NOT "OK" OR "NOT OK" YOU MORON
		        if (reply.equalsIgnoreCase("1")) {
		        	// Send "transaction aborted" string to client
		        	sendData = (itemID + " transaction aborted").getBytes();
		        	sendPacket = new DatagramPacket(sendData, sendData.length, 
							replyIPAddress, replyPort);
		        	// Simulate packet loss
		        	x = Math.random();
		        	if (x < PACKET_LOSS_SIM) {
		        		serverSocket.send(sendPacket);
		        	}

		        	// Set timeout to defined amount of time
		        	serverSocket.setSoTimeout(TIMEOUT);
		        	receiveData = new byte[1024];
		        	receivePacket = new DatagramPacket(receiveData, 
		        			receiveData.length);

		        	// Wait for ACK
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
		        		break;
		        	}
		        } else {
		        	// Contact Content and retrieve content
		        	sendData = ("" + itemID).getBytes();
		        	sendPacket = new DatagramPacket(sendData, sendData.length, 
		    				content.getAddress(), content.getPort());
		        	
		        	// Simulate packet loss
		        	x = Math.random();
		        	if (x < PACKET_LOSS_SIM) {
		        		serverSocket.send(sendPacket);
		        	}

		        	// Set timeout to defined amount of time
		        	serverSocket.setSoTimeout(TIMEOUT);
		        	receiveData = new byte[1024];
		        	receivePacket = new DatagramPacket(receiveData, 
		        			receiveData.length);

		        	// Wait for ACK
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
		        		break;
		        	}
					
		        	// Wait for a reply from content
		        	receiveData = new byte[1024];
					receivePacket = new DatagramPacket(receiveData, 
							receiveData.length);
					serverSocket.receive(receivePacket);
					
					// Send ACK
					sendData = "ACK".getBytes();
					sendPacket = new DatagramPacket(sendData, sendData.length, 
					        content.getAddress(), content.getPort());
					serverSocket.send(sendPacket);
					
					// Convert request to a String
					reply = new String(receivePacket.getData());
					reply = reply.trim();
					replySplit = reply.split(" ");
					
			        if (replySplit.length > 1) {
			        	// Failed
			        	sendData = (itemID + " transaction aborted").getBytes();
			        	sendPacket = new DatagramPacket(sendData, 
			        			sendData.length, replyIPAddress, replyPort);
			        	
			        	// Simulate packet loss
			        	x = Math.random();
			        	if (x < PACKET_LOSS_SIM) {
			        		serverSocket.send(sendPacket);
			        	}

			        	// Set timeout to defined amount of time
			        	serverSocket.setSoTimeout(TIMEOUT);
			        	receiveData = new byte[1024];
			        	receivePacket = new DatagramPacket(receiveData, 
			        			receiveData.length);

			        	// Wait for ACK
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

			        	// If no ACK response comes after RETRIES number of 
			        	// times, assume server is offline and end
			        	if (attempts >= RETRIES) {
			        		System.err.println("No response, server offline.");
			        		break;
			        	}
			        } else {
			        	// Give content to client
			        	sendData = (itemID + " " + stockData.get(itemID) + " " + 
			        	replySplit[0]).getBytes();
			        	sendPacket = new DatagramPacket(sendData, 
			        			sendData.length, replyIPAddress, replyPort);
			        	
			        	// Simulate packet loss
			        	x = Math.random();
			        	if (x < PACKET_LOSS_SIM) {
			        		serverSocket.send(sendPacket);
			        	}

			        	// Set timeout to defined amount of time
			        	serverSocket.setSoTimeout(TIMEOUT);
			        	receiveData = new byte[1024];
			        	receivePacket = new DatagramPacket(receiveData, 
			        			receiveData.length);

			        	// Wait for ACK
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

			        	// If no ACK response comes after RETRIES number of 
			        	// times, assume server is offline and end
			        	if (attempts >= RETRIES) {
			        		System.err.println("No response, server offline.");
			        		break;
			        	}
			        }
		        }		        			
	    	}	
        }
        serverSocket.close();
	}
}
