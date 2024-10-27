import java.io.*;
import java.net.ServerSocket;  // The server uses this to bind to a port
import java.net.Socket;        // Incoming connections are represented as sockets
import java.util.ArrayList;

//json reading libraries
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;


/**
 * Marian Zaki (COSC 2454)
 * A simple server class.  Accepts client connections and forks
 * EchoThreads to handle the bulk of the work.
 *
 * 
 */
public class EchoServer
{
    /** The server will listen on this port for client connections */
    public static int SERVER_PORT;
	public static ArrayList<ServerInfo> serverList = new ArrayList<>(); //use this alicia


	private static SinglyLinkedList<Integer> sl = new SinglyLinkedList<Integer>();

	public static SinglyLinkedList<Integer> getList(){
		return sl;
	}
	public static void setList(SinglyLinkedList<Integer> l){
		sl = l;
	}


	/**
     * Main routine.  Just a dumb loop that keeps accepting new
     * client connections.
     *
     */
    public static void main(String[] args){

		if(args.length != 1)
		{
			System.err.println("Not enough arguments.\n");
			System.err.println("Usage:  java EchoServer <Server Config json>\n");
			System.exit(-1);
		}


		try{

			JSONParser jPars = new JSONParser(); //parser to read json file
			JSONObject jServers = (JSONObject) jPars.parse(new FileReader(args[0])); //putting it into json array
			JSONArray jArr = (JSONArray) jServers.get("servers");

			for (Object ob : jArr){ //looping through the json array for each entry

				JSONObject jOb = (JSONObject) ob;

				String name = (String) jOb.get("name");
				String ip = (String) jOb.get("ip");
				long portLong = (long) jOb.get("port");
				//System.out.println(portLong);
				int port = Math.toIntExact(portLong);
				//System.out.println(port);

				serverList.add(new ServerInfo(name, ip, port)); //putting it into serverList
			}


			ServerSocket serverSock = null;

			boolean endOfList = true; //boolean to check if it went through the entire list
			for (ServerInfo p : serverList){ //loops through serverList array
				try{
					SERVER_PORT = p.port;
					serverSock = new ServerSocket(SERVER_PORT);
					System.out.println("Server started successfully on port " + SERVER_PORT + " .... ");
					endOfList = false; //flips variable if successful
					break; //breaks for loop if successful
				}
				catch(Exception connectionEx){ //error if fails to connect (can be because it's taken or other reasons)
					System.err.println("Error: " + connectionEx.getMessage() + " on port " + p.port);
					//connectionEx.printStackTrace(System.err);
				}
			}
			if(endOfList){ //if it got to the end of the list with no successful connection
				System.out.println("All server ports occupied. Cannot start new server.");
				System.exit(0); //exits the program
			}


			boolean didSync = false;
			for (ServerInfo element: EchoServer.serverList) { //for-each loop to loop through all ports in the json
				if(element.port != EchoServer.SERVER_PORT) {
					try {
						Logs log = Logs.getInstance();
						log.writeServer("<" + serverSock.getInetAddress() + " Startup connection to "
								+ element.name + " - port: " + element.port + ">");
						System.out.println("< Startup connection to " + element.name + " - port: " + element.port + ">");

						final Socket sock = new Socket(element.ip, element.port); //attempts connection
						System.out.println("Connecting to " + element.ip + " on port " + element.port +
								" LocalPort number is: " + sock.getLocalPort()); //if connection is successful
						System.out.println("< Connecting to " + element.name + " - port: " + element.port + ">");


						log.writeServer("<" + serverSock.getInetAddress() + " Connection to "
								+ element.name + " - port: " + element.port + " was successful! >");
						System.out.println("Connection to " + element.name + " - port: " + element.port + "was successful! >");


						// Set up I/O streams with the server
						final ObjectOutputStream tempOut = new ObjectOutputStream(sock.getOutputStream());
						final ObjectInputStream tempIn = new ObjectInputStream(sock.getInputStream());

						Message cmd = new Message("get list");
						tempOut.writeObject(cmd); //sends command to server

						log.writeServer("<" + serverSock.getInetAddress() + " Sent command to "
								+ element.name + " - port: " + element.port + ">");
						System.out.println("Sent command to " + element.name + " - port: " + element.port + ">");


						setList((SinglyLinkedList<Integer>) tempIn.readObject()); //sets the linked list
						log.writeServer("<" + serverSock.getInetAddress() + " Linked list synced with "
								+ element.name + " - port: " + element.port + ">");
						System.out.println("Linked list synced with " + element.name + " - port: " + element.port + ">");


						tempOut.writeObject(new Message("EXIT")); //closes connection to the server
						sock.close();
						log.writeServer("<" + serverSock.getInetAddress() + " Closed connection to "
								+ element.name + " - port: " + element.port + ">");
						System.out.println("Closed connection to " + element.name + " - port: " + element.port + ">");

						didSync = true;
						break;

					} catch (Exception e) {
						System.out.println("Something Went Wrong: Server unable to send Message");
						System.err.println("Error: " + e.getMessage());
					}
				}
			}
			if(!didSync){
				try{
					FileInputStream fileIn = new FileInputStream("LinkedListSer.ser");
					ObjectInputStream fileInput = new ObjectInputStream(fileIn);

					SinglyLinkedList<Integer> deserList = (SinglyLinkedList<Integer>)fileInput.readObject();
					EchoServer.setList(deserList);

					fileInput.close();
					fileIn.close();

					System.out.println("Data Structure loaded back successfully!");
				}
				catch(Exception e) {
					System.out.println("Error: no file to deserialize");
				}
			}




			// A simple infinite loop to accept connections
			Socket sock = null;
			EchoThread thread = null;
			while(true){
					System.out.println("Waiting for a new connection .... ");
			sock = serverSock.accept();     // Accept an incoming connection
			thread = new EchoThread(sock);  // Create a thread to handle this connection
			thread.start();                 // Fork the thread
			}                                   // Loop to work on new connections while this
													// the accept()ed connection is handled


		}
		catch(Exception e){
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace(System.err);
		}

    }  //-- end main(String[])

} //-- End class EchoServer