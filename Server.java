import java.awt.BufferCapabilities.FlipContents;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server {

    public static void main(String[] args) {
    	
    	//The server socket that will branch off connections
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(16000);
        }catch (IOException e){
            System.out.println("Error");
            return;
        }

        //Running the server
        while(true) {
            try {
                System.out.println("Trying to connect...");
                Socket connection = serverSocket.accept();
                
                //Creating a new thread if there is a connection
                ConnectionThread connectionThread = new ConnectionThread(connection);
                connectionThread.run();
                 
            } catch (IOException e) {
                System.out.println("Error");
                return;
            }
        }
    }
}
