import java.io.*;
import java.lang.Thread;            // We will extend Java's base Thread class
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;


/**
 * Marian Zaki (COSC 2454)
 * A simple server thread.  This class just echoes the messages sent
 * over the socket until the socket is closed.
 *
 */
public class EchoThread extends Thread {
	private final Socket socket;                   // The socket that we'll be talking over
	private boolean sendToServer = false;
	private int count = 0;

	/**
	 * Constructor that sets up the socket we'll chat over
	 *
	 * @param socket The socket passed in from the server
	 */
	public EchoThread(Socket socket) {
		this.socket = socket;
	}

	/**
	 * run() is basically the main method of a thread.  This thread
	 * simply reads Message objects off of the socket.
	 */
	public void run() {
		try {
			// Print incoming message
			System.out.println("** New connection from " + socket.getInetAddress() + ":"
					+ socket.getPort() + " **");

			// set up I/O streams with the client
			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

			// Loop to read messages
			Message msg = null;

			do {
				// read and print message
				msg = (Message) input.readObject();


				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] " + msg.theMessage);

				//command to lowercase
				String messageString = msg.theMessage.toLowerCase();
				//making copy of command if we need to send to other servers later
				String messageCopy = messageString;


				if (messageString.contains("add")) {
					addInt(messageString, output);

				} else if (messageString.contains("remove")) {
					removeInt(messageString, output);

				} else if (messageString.contains("insert")) {
					insert(messageString, output);

				} else if (messageString.contains("view")) {
					output.writeObject(new Message(EchoServer.getList().printList()));
					Logs log = Logs.getInstance();
					log.writeLog("<" + socket.getInetAddress() + " view " + ">");

				} else if (messageString.contains("log")) {
					viewLog(messageString, output);
					Logs log = Logs.getInstance();
					log.writeLog("<" + socket.getInetAddress() + " log " + ">");

				} else if (messageString.contains("commit")) { //placeholder for commit
					commit(messageString, output);
					Logs log = Logs.getInstance();
					log.writeLog("<" + socket.getInetAddress() + " commit " + ">");

				} else if (messageString.contains("rollback")) { //placeholder for rollback
					rollback(messageString, output);
					Logs log = Logs.getInstance();
					log.writeLog("<" + socket.getInetAddress() + " rollback " + ">");
				} else if (messageString.contains("get list")) { //server only command
					output.writeObject(EchoServer.getList());
				}

				//if boolean is true, then command needs to be sent to other servers
				//use messageCopy
				if (sendToServer) {
					String variableSend = messageCopy + " server " + EchoServer.SERVER_PORT; //adding server credentials to command
					for (ServerInfo element : EchoServer.serverList) { //for-each loop to loop through all ports in the json
						if (element.port != EchoServer.SERVER_PORT) {
							try {
								Logs log = Logs.getInstance();
								log.writeServer("<" + socket.getInetAddress() + " Attempting connection to "
										+ element.name + " - port: " + element.port + ">");
								System.out.println("< Attempting connection to " + element.name + " - port: " + element.port + ">");

								final Socket sock = new Socket(element.ip, element.port); //attempts connection
								System.out.println("Connecting to " + element.ip + " on port " + element.port +
										" LocalPort number is: " + sock.getLocalPort()); //if connection is successful
								System.out.println("< Connecting to " + element.name + " - port: " + element.port + ">");


								log.writeServer("<" + socket.getInetAddress() + " Connection to "
										+ element.name + " - port: " + element.port + " was successful! >");
								System.out.println("Connection to " + element.name + " - port: " + element.port + "was successful! >");


								// Set up I/O streams with the server
								final ObjectOutputStream tempOut = new ObjectOutputStream(sock.getOutputStream());
								final ObjectInputStream tempIn = new ObjectInputStream(sock.getInputStream());

								Message cmd = new Message(variableSend);
								tempOut.writeObject(cmd); //sends command to server

								log.writeServer("<" + socket.getInetAddress() + " Sent command to "
										+ element.name + " - port: " + element.port + ">");
								System.out.println("Sent command to " + element.name + " - port: " + element.port + ">");

								//wait for response from the server
								Message response = (Message) tempIn.readObject();
								System.out.println(response.theMessage);

								tempOut.writeObject(new Message("EXIT")); //closes connection to the server

								tempOut.close();
								sock.close();
								log.writeServer("<" + socket.getInetAddress() + " Closed connection to "
										+ element.name + " - port: " + element.port + ">");
								System.out.println("Closed connection to " + element.name + " - port: " + element.port + ">");

								sendToServer = false;

							} catch (Exception e) {
								System.out.println("Something Went Wrong: Server unable to send Message");
								System.err.println("Error: " + e.getMessage());
							}
						}
					}

				}


				// Write an ACK back to the sender
				count++;

			} while (!msg.theMessage.toUpperCase().equals("EXIT"));




			// Close and cleanup
			System.out.println("** Closing connection with " + socket.getInetAddress() + ":" + socket.getPort() + " **");
			socket.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace(System.err);
		}

	}  //-- end run()

	//Maden's Coding YAY!/ We will test this one first!
	private void addInt(String command, ObjectOutputStream output) {
		command = command.replace("add ", "");
		if(command.contains("server")){ //server log
			try {
				String[] substring = command.split(" ");
				String portNumber = substring[2];
				int number = Integer.parseInt(substring[0]); // turns the nu
				//appending the number to the end of the Linked List
				EchoServer.getList().append(new Node(number));
				Logs log = Logs.getInstance();
				log.writeServer("<" + socket.getInetAddress() + " add: " + number + "," + " request from server port: " + portNumber + ">");

				try {
					output.writeObject(new Message("Command #" + count + " Received, Successfully added to Singly Linked List"));
				} catch (Exception ex) {
					System.err.println("Error: " + ex.getMessage());
					ex.printStackTrace(System.err);
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
				try {
					output.writeObject(new Message("!!! ERROR: Command not formatted correctly !!!"));
				} catch (Exception ex) {
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace(System.err);
				}
			}
		}
		else{ //client log
			try {
				int number = Integer.parseInt(command); // turns the nu
				//appending the number to the end of the Linked List
				EchoServer.getList().append(new Node(number));
				Logs log = Logs.getInstance();
				log.writeLog("<" + socket.getInetAddress() + " add: " + number + ">");
				sendToServer = true;
				try {
					output.writeObject(new Message("Command #" + count + " Received, Successfully added to Singly Linked List"));
				} catch (Exception ex) {
					System.err.println("Error: " + ex.getMessage());
					ex.printStackTrace(System.err);
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
				try {
					output.writeObject(new Message("!!! ERROR: Command not formatted correctly !!!"));
				} catch (Exception ex) {
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace(System.err);
				}
			}
		}
	}
	///read teh string from input line.. parse it.. concantenat nubmer string to integer
	//Commenting out, we will be testing these individually starting with add! Maden/Alicia wrote this!
	private void removeInt(String command, ObjectOutputStream output) {
		command = command.replace("remove ", "");
		if (command.contains("server")) { //server log
			try {
				String[] substring = command.split(" ");
				String portNumber = substring[2];
				int number = Integer.parseInt(substring[0]); // turns the nu

				EchoServer.getList().remove(number);
				Logs log = Logs.getInstance();
				log.writeServer("<" + socket.getInetAddress() + " remove: " + number + ", " + "  request from server port: " + portNumber + ">");

				try {
					output.writeObject(new Message("Command #" + count + " Received, Successfully removed from Singly Linked List"));
				} catch (Exception ex) {
					System.err.println("Error: " + ex.getMessage());
					ex.printStackTrace(System.err);
				}

			} catch (NumberFormatException e) {
				try {
					output.writeObject(new Message("!!! ERROR: Command not formatted correctly !!!"));
				} catch (Exception ex) {
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace(System.err);
				}
			}

		}
		else{ //client log
			//if user just wants to remove from the end of list
			if (command.equals("")) {
				EchoServer.getList().remove();

			}
			else {
				try {
					Integer number = Integer.parseInt(command);
					EchoServer.getList().remove(number);
					Logs log = Logs.getInstance();
					log.writeLog("<" + socket.getInetAddress() + " delete: " + number + ">");
					sendToServer = true;
					try {
						output.writeObject(new Message("Command #" + count + " Received, Successfully removed from Singly Linked List"));
					} catch (Exception ex) {
						System.err.println("Error: " + ex.getMessage());
						ex.printStackTrace(System.err);
					}

				}
				catch (NumberFormatException e) {
					try {
						output.writeObject(new Message("!!! ERROR: Command not formatted correctly !!!"));
					} catch (Exception ex) {
						System.err.println("Error: " + e.getMessage());
						e.printStackTrace(System.err);
					}
				}
			}
		}
	}
	private void viewLog(String command, ObjectOutputStream output) {
		command = command.replace("log ", "");
		try {
			//FileInputStream fis = new FileInputStream("log.txt");
			String temp = "";

			File file = null;
			if(command.equals("client")){ //checks if they want to view client logs
				file = new File("client_log.txt");
				System.out.println("Showing Client log");
			}
			else if(command.equals("server")){//checks if they want to view server logs
				file = new File("server_log.txt");
				System.out.println("Showing Servers log");
			}
			else{
				output.writeObject(new Message("!!! Error: please choose between \"client\" or \"server\" log"));
				return; //ends command if there is an error
			}

			Scanner sc = new Scanner(file);
			while (sc.hasNextLine()) {
				temp += sc.nextLine() + "\n";

			}
			output.writeObject(new Message(temp));
			sc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void insert(String command, ObjectOutputStream output) {

		command = command.replace("insert ", "");
		String[] commandList = command.split(" ");
		if (command.contains("server")) {
			try {

				String portNumber = commandList[3];
//				System.out.print(command); //testing purposes
//				System.out.print(commandList); //testing purposes
				int number = Integer.parseInt(commandList[0]);
				System.out.print(number); //testing purposes
				Integer index = Integer.parseInt(commandList[1]);
				EchoServer.getList().insert(new Node<Integer>(number), index);
				System.out.print(index);
				Logs log = Logs.getInstance();
				log.writeServer("<" + socket.getInetAddress() + " insert: " + number + "," + portNumber + ">");

				try {
					output.writeObject(new Message("Command #" + count + " Received, Successfully allowing client to view Singly Linked List"));
				} catch (Exception ex) {
					System.err.println("Error: " + ex.getMessage());
					ex.printStackTrace(System.err);
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
				try {
					output.writeObject(new Message("!!! ERROR: Command not formatted correctly !!!"));
				} catch (Exception ex) {
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace(System.err);
				}


			}
		}
		else{
			try {
				int number = Integer.parseInt(commandList[0]);
//				System.out.print(number);
				int index = Integer.parseInt(commandList[1]);
				EchoServer.getList().insert(new Node<Integer>(number), index);
				System.out.print(index);
				Logs log = Logs.getInstance();
				log.writeLog("<" + socket.getInetAddress() + " insert: " + number + ">");
				sendToServer = true;
				try {
					output.writeObject(new Message("Command #" + count + " Received, Successfully allowing client to view Singly Linked List"));
				} catch (Exception ex) {
					System.err.println("Error: " + ex.getMessage());
					ex.printStackTrace(System.err);
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
				try {
					output.writeObject(new Message("!!! ERROR: Command not formatted correctly !!!"));
				} catch (Exception ex) {
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace(System.err);
				}

			}
		}
	}


	private void commit(String command, ObjectOutputStream output){
		try{
			FileOutputStream fileOut = new FileOutputStream("LinkedListSer.ser");
			ObjectOutputStream fileOutput = new ObjectOutputStream(fileOut);

			fileOutput.writeObject(EchoServer.getList());

			fileOutput.close();
			fileOut.close();

			try {
				output.writeObject(new Message("Data Structure commit successfully!"));
			} catch (IOException e) {
				System.out.println();
			}

		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				output.writeObject(new Message("!!! ERROR: Could not commit to disk !!!"));
			} catch (Exception ex) {
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
		//checks whether the command from a server or a client and logs them appropriately
		if (command.contains("server")){
			String[] substring = command.split(" ");
			Logs log = Logs.getInstance();
			log.writeServer("<" + socket.getInetAddress() + " Commited data structure. Request made by port: "+ substring[2] + ">");
		}
		else{
			sendToServer = true;
			Logs log = Logs.getInstance();
			log.writeLog("<" + socket.getInetAddress() + " Commited data structure." + ">");
		}
	}

	private void rollback(String command, ObjectOutputStream output){
		try{
			FileInputStream fileIn = new FileInputStream("LinkedListSer.ser");
			ObjectInputStream fileInput = new ObjectInputStream(fileIn);

			SinglyLinkedList<Integer> deserList = (SinglyLinkedList<Integer>)fileInput.readObject();
			EchoServer.setList(deserList);

			fileInput.close();
			fileIn.close();

			try {
				output.writeObject(new Message("Data Structure rolled back successfully!"));
			} catch (IOException e) {
				System.out.println();
			}

		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				output.writeObject(new Message("!!! ERROR: Could not roll back data structure !!!"));
			} catch (Exception ex) {
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
		//checks whether the command from a server or a client and logs them appropriately
		if (command.contains("server")){
			String[] substring = command.split(" ");
			Logs log = Logs.getInstance();
			log.writeServer("<" + socket.getInetAddress() + " Commited data structure. Request made by port: "+ substring[2] + ">");
		}
		else{
			sendToServer = true;
			Logs log = Logs.getInstance();
			log.writeLog("<" + socket.getInetAddress() + " Commited data structure." + ">");

		}
	}





} //-- end class EchoThread