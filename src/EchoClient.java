import java.io.*;
import java.net.Socket;             // Used to connect to the server
import java.util.Scanner;

/**
 * Marian Zaki (COSC 2454)
 * Simple client class.  This class connects to an EchoServer to send
 * text back and forth.  Java message serialization is used to pass
 * Message objects around.
 *
 */
public class EchoClient
{
    /**
     * Main method.
     * @param args  First argument specifies the server to connect to
     */
    public static void main(String[] args)
    {
	// Checking for error in arguments

		/*
		if(args.length != 2)
        {
            System.err.println("Not enough arguments.\n");
            System.err.println("Usage:  java EchoClient <Server name or IP> <Server Port Number\n");
            System.exit(-1);
        }
		 */

		Scanner scanObj = new Scanner(System.in);

	try{
	    // Connect to the specified server
			System.out.println("Enter server ip:");
            String serverIP = scanObj.next();
			System.out.println("Enter server port:");
            int serverPort = scanObj.nextInt();
            
	    final Socket sock = new Socket(serverIP, serverPort);
	    System.out.println("Connected to " + serverIP + " on port " + serverPort + 
                    " LocalPort number is: " + sock.getLocalPort());
	    
	    // Set up I/O streams with the server
	    final ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
	    final ObjectInputStream input = new ObjectInputStream(sock.getInputStream());

	    // loop to send messages
	    Message msg = null, resp = null;
            Message resp2 = null;
	    do{
		// Read and send message.  Since the Message class
		// implements the Serializable interface, the
		// ObjectOutputStream "output" object automatically
		// encodes the Message object into a format that can
		// be transmitted over the socket to the server.
		msg = new Message(readSomeText());
		output.writeObject(msg);


		// Get ACK and print.  Since Message implements
		// Serializable, the ObjectInputStream can
		// automatically read this object off of the wire and
		// encode it as a Message.  Note that we need to
		// explicitly cast the return from readObject() to the
		// type Message.
		resp = (Message)input.readObject();
		System.out.println("\nServer says: " + resp.theMessage + "\n");
                                
	    }while(!msg.theMessage.toUpperCase().equals("EXIT"));
	    
	    // shut things down
	    sock.close();

	}
	catch(Exception e){
	    System.err.println("Error: " + e.getMessage());
	    e.printStackTrace(System.err);
	}

    } //-- end main(String[])

    /**
     * Simple method to print a prompt and read a line of text.
     *
     * @return A line of text read from the console
     */
    private static String readSomeText()
    {
		try{
			System.out.println("Enter a command:" +
					"\n    view - prints data structure" +
					"\n    log (client / server) - prints the client or server log for the server" +
					"\n    add (number) - adds number to the end of the list" +
					"\n    remove (index) - removes the index from the list" +
					"\n    insert (number) (index) - inserts a number at the given index" +
					"\n    commit - saves current data structure" +
					"\n    rollback - loads previously saved data structure" +
					"\n    EXIT - closes the client" +
					"\n    ");
			System.out.print(" > ");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			return in.readLine();
		}
		catch(Exception e){
			// Uh oh...
			return "";
		}
    } //-- end readSomeText()




} //-- end class EchoClient