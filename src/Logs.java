import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
public class Logs {
    //create an object of Logger
    private File currentLogFile;
    private static Logs instance = new Logs();

    //make the constructor private so that the class cant be instantiated
    private Logs() {}
    public static Logs getInstance() {
        if (instance == null) {
            // Ensure that the instance hasn't yet been
            // initialized by another thread while this one
            // has been waiting for the lock's release.
            synchronized (Logs.class) {
                if (instance == null) {
                    instance = new Logs();
                }
            }
        }
        return instance;
    }

    public void showMessage() {
            System.out.println("Hello World!");
        }

        /*
        The writeLog function will take a string and appends that string to the log file in a new line
        that will be prepended by the current system timestamp. For example, if I execute the

        Then the following string will be added to the log file: 01/10/2024: 02:43:02 – Username: lee20,
        logged in system for 20 minutes
        */

    public void writeLog (String newLogEntry) {
        String filepath = "client_log.txt";
        try {
            File file = new File(filepath);
            if(!file.exists())
                file.createNewFile();

            FileWriter writer = new FileWriter(filepath, true);
            //get current time stamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            BufferedWriter bw = new BufferedWriter(writer);
            //add log message with the time stamp
            writer.write(newLogEntry + " " + timestamp);
            bw.newLine();
            bw.close();
            writer.close();

        }catch (IOException e){
            System.out.print(e);
        }
    }


    //Method for writing server
    public void writeServer(String newLogEntry) {
        String filepath = "server_log.txt";
        try {
            File file = new File(filepath);
            if(!file.exists())
                file.createNewFile();

            FileWriter writer = new FileWriter(filepath, true);
            //get current time stamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            BufferedWriter bw = new BufferedWriter(writer);
            //add log message with the time stamp
            writer.write(newLogEntry + " " + timestamp);
            bw.newLine();
            bw.close();
            writer.close();

        }catch (IOException e){
            System.out.print(e);
        }
    }




        /*
        This function should create a new log file with the specified name and path as given to the
        function argument. Once the log file is created it becomes the default file that writeLog
        will be writing into. This means that your program can create multiple log files but writeLog will
        always be writing into the last created file
        */
        public File createLog(String filepath){
            try {
                File newLogFile = new File(filepath);
                //if file doesnt exist
                if (newLogFile.createNewFile()) {
                    System.out.println("New log file created: " + filepath);
                } else {
                    System.out.println("Log file already exists: " + filepath);
                }
                currentLogFile = newLogFile; //updates current log file
            } catch (IOException e) {
                System.out.println("Error creating log file: " + e.getMessage());
            }
            return currentLogFile;

        }

}

