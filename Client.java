import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by rajanjassal on 16-03-23.
 */
public class Client {
    public static void main(String[] args) {
    
    	//Variables that link to a valid username and key
    	long userID;
    	long[] key;
    	String inputFileName;
    	
    	//Key used to terminate the connection
    	long[] killKey = {78,933,12,567};
    	
    	//Server name
        String serverName = "localhost";
        
        //Used to generate random numbers
        Random random = new Random();
        
        //Stores a list of all valid keys and ID's
        HashMap<Long, long[]> users = new HashMap<>();
        long userID1 = 1;
        long userID2 = 2;
        
        //Note user 3 is not on the server for testing purposes
        long userID3 = 3;
        long[] key1 = {1,2,3,4};
        long[] key2 = {5,6,7,8};
        long[] key3 = {55,66,77,87};
        users.put(userID1, key1);
        users.put(userID2, key2);
        users.put(userID3, key3);
        //Getting a userId from the user
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a userID to log in(only numbers):");
        long enteredUser = scanner.nextLong(); 
        
        //Retrying getting the users
        while(!users.containsKey(enteredUser)){
        	System.out.println("Not a valid user please try again:");
        	enteredUser = scanner.nextLong(); 
        }
        
        //Geting a valid user
        System.out.println("Valid user entered.");
        userID = enteredUser;
        key = users.get(userID);
        
        //Array used for encryption to send the user ID
        long[] userIDEncryption = {userID, random.nextLong()};
        
        
         
        try {
        	Encryption encryption = new Encryption();
        	System.loadLibrary("encryption");
        	
            System.out.println("Attempting to connect...");
            Socket socket = new Socket(serverName, 16000);
            System.out.println("Connected to "+ socket.getRemoteSocketAddress());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            
            //Encrypt userID
            encryption.encrypt(userIDEncryption, key);
            outputStream.writeLong(userIDEncryption[0]);
            outputStream.writeLong(userIDEncryption[1]);
            
            //Checking the response
            long ackRepsone = inputStream.readLong();
            
            //If not registered message is sent
            if(ackRepsone == 'N'){
            	System.out.println("Not registered to Server. Closing connection");
            	socket.close();
            	return;
            }
            
            //Getting the random value for decryption if you are registered
            long randomNum = inputStream.readLong();
            long[] response = {ackRepsone, randomNum};
            encryption.decrypt(response, key);
            
            
            if(response[0] == 'Y'){
            	System.out.println("Registered to Server.");
            }else{
            	System.out.println("Registation failed. Ack not recieved");
            	socket.close();
            	return;
            }
            
            //File request loop
            while(true){
	            
            	//Getting a file name from the user
            	System.out.println("Please enter a fileName or E to exit");
            	
	            inputFileName = scanner.next();
	            
	            //User exits so kill the connection
	            if(inputFileName.equals("E")){
	            	System.out.println("Ending connection");
	            	long[] sendKill = {0, random.nextLong()};
		            encryption.encrypt(sendKill, killKey);
		            outputStream.writeLong(sendKill[0]);
		            outputStream.writeLong(sendKill[1]);
	            	scanner.close();
	            	socket.close();
	            	return;
	            }
	            
	            //Sending the file name
	            char[] charFileName = inputFileName.toCharArray();
	            
	            //Sending the length of the filename
	            int charFileNameLenght = charFileName.length;
	            long[] sendFileNameLength = {charFileNameLenght, random.nextLong()};
	            encryption.encrypt(sendFileNameLength, key);
	            outputStream.writeLong(sendFileNameLength[0]);
	            outputStream.writeLong(sendFileNameLength[1]);
	            
	            //Sending the fileName
	            for(char letter : charFileName){
	            	long[] sendLetter = {letter, random.nextLong()};
	            	encryption.encrypt(sendLetter, key);
	            	outputStream.writeLong(sendLetter[0]);
	            	outputStream.writeLong(sendLetter[1]);	
	            }
	            
	            //Waiting to see if the server has the file
	            ackRepsone = inputStream.readLong();
	            randomNum = inputStream.readLong();
	            long[] hasFileResponse = {ackRepsone, randomNum};
	            encryption.decrypt(hasFileResponse, key);
	            
	            if((char)hasFileResponse[0] == 'N'){
	            	System.out.println("Server does not have the file.");
	            	
	            	continue;
	            }else if((char)hasFileResponse[0] == 'Y'){ 
	            	System.out.println("Server has the file. Reading file now");
	            }else{
	            	System.out.println("Error in response");
	            	
	            	continue;
	            }
	            
	            //Getting the file size being sent
	            ackRepsone = inputStream.readLong();
	            randomNum = inputStream.readLong();
	            long[] fileSize = {ackRepsone, randomNum};
	            encryption.decrypt(fileSize, key);
	            
	            long fileSizeRecieved = fileSize[0];
	            
	           
	            
	            //Receiving the file and then writing it to a file
	            try{
	            	FileWriter fileWriter = new FileWriter(inputFileName);
	            	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	            	
	            	for(int i = 0; i<fileSizeRecieved;i++){
	                    ackRepsone = inputStream.readLong();
	                    randomNum = inputStream.readLong();
	                    long[] charReceived = {ackRepsone, randomNum};
	                    encryption.decrypt(charReceived, key);
	                    bufferedWriter.write((char)charReceived[0]);
	            	}
	            	bufferedWriter.close();
	            }catch(IOException e){
	            	System.out.println("Error writing to the file");
	            	
	            	continue;
	            }
	            
	            System.out.println("Recieved file");
				
	        
	           
	            continue;
	            }
        }
    
        catch(IOException e){
            System.out.println("Error in connection. Exiting the program");
            return;
        }
    
    }
}
