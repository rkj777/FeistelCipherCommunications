import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ConnectionThread extends Thread {
	Socket connection;
	long userID;
	HashMap<Long, long[]> userKeyList = new HashMap<>();
	long[] key1 = {1,2,3,4};
    long[] key2 = {5,6,7,8};
	Random random = new Random();
	
	//Key used to terminate the connection
	long[] killKey = {78,933,12,567};
	
	
	public ConnectionThread(Socket connection){
		//Setting the inputed connection
		this.connection = connection;
		
		//Creating the list of users and keys
		userKeyList.put((long) 1, key1);
		userKeyList.put((long) 2, key2);
	}
	
	@Override
	public void run(){
		
		Encryption encryption = new Encryption();
		System.loadLibrary("encryption");
		
		try {
			
			//Setting the streams to the client
            System.out.println("Just connected to " + connection.getRemoteSocketAddress());
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            DataInputStream inputStream = new DataInputStream(connection.getInputStream());
            userID = inputStream.readLong();
         
            //See the user id is valid
            long randomValue = inputStream.readLong();
            boolean match = false;
            long[] key = {};
            for(Map.Entry<Long, long[]> user: userKeyList.entrySet() ){
            	long[] userIdDecrypt = {userID,randomValue};
            	encryption.decrypt(userIdDecrypt, user.getValue());
            	if(userIdDecrypt[0] == user.getKey()){
            		match = true;
            		key = user.getValue();
            		break;
            	}
            }
            
            //Sending back a fail
            if(!match){
            	System.out.println("The client is not registered. Closing connection");
            	outputStream.writeLong('N');
            	connection.close();
            	return;
            }else{
            	
            	//Sending ack that client is recognized
            	System.out.println("Recongnizing client. Sending Ack");
            	long ackMessage = 'Y';
            	
            	long[] ackSend = {ackMessage, random.nextLong()};
            	encryption.encrypt(ackSend, key);
            	
                outputStream.writeLong(ackSend[0]);
                outputStream.writeLong(ackSend[1]);
                
            	
            }
            
           while(true){
           //Receiving the filenameLength
           System.out.println("Waiting for filename");
           long fileNameLength = inputStream.readLong();
           randomValue = inputStream.readLong();
           long[] fileNameLengthArray = {fileNameLength, randomValue};
           long[] killSignal = {fileNameLength, randomValue};
           
           //Check if kill signal was sent
           encryption.decrypt(killSignal,killKey);
           long kill = killSignal[0];
           
           //Killing the thread
           if(kill == 0){
        	   connection.close();
        	   System.out.println("User has closed the connection");
        	   return;
           }
           
           //Getting the file name length
           encryption.decrypt(fileNameLengthArray,key);
           fileNameLength = fileNameLengthArray[0];
           
           //Getting the actual filename
           String fileName = "";
           for(int i = 0; i<fileNameLength;i++){
        	   long letter = inputStream.readLong();
        	   randomValue = inputStream.readLong();
        	   long[] letterDecode = {letter,randomValue};
        	   encryption.decrypt(letterDecode, key);
        	   fileName += (char)letterDecode[0];
           }
          
           System.out.println("Received the filename " + fileName);
           ArrayList<Character> characterFile = new ArrayList<>();
           
           //Trying to read the file
           try{
        	   FileReader fileReader = new FileReader(fileName);
        	   BufferedReader bufferedReader = new BufferedReader(fileReader);
        	   int readLetter;
        	   while((readLetter = bufferedReader.read()) != -1){
        		   characterFile.add((char) readLetter);
        	   }
        	   bufferedReader.close();
        	   
           }catch(FileNotFoundException e){
        	   System.out.println("File not found");
        	  
        	   long ackMessage = 'N';
               long[] ackSend = {ackMessage, random.nextLong()};
               encryption.encrypt(ackSend, key);
               outputStream.writeLong(ackSend[0]);
               outputStream.writeLong(ackSend[1]);
               
        	   continue;
           }catch(IOException e){
        	   System.out.println("Error reading the file");
        	   
           	   long ackMessage = 'N';
               long[] ackSend = {ackMessage, random.nextLong()};
               encryption.encrypt(ackSend, key);
               outputStream.writeLong(ackSend[0]);
               outputStream.writeLong(ackSend[1]);
               
        	   continue;
           }
           
           //Sending an ack that the file exist
           System.out.println("File exists and is read. Sending Ack");
           
           long ackMessage = 'Y';
           long[] ackSend = {ackMessage, random.nextLong()};
           encryption.encrypt(ackSend, key);
           outputStream.writeLong(ackSend[0]);
           outputStream.writeLong(ackSend[1]);
			
           //Sending the size of the file
           long fileSize =  characterFile.size();
           System.out.println(fileSize);
           long[] fileSizeSend = {fileSize, random.nextLong()};
           encryption.encrypt(fileSizeSend, key);
           outputStream.writeLong(fileSizeSend[0]);
           outputStream.writeLong(fileSizeSend[1]);
           
           //Sending file
           System.out.println("Sending the file");
           for(char letter: characterFile){
        	   long letterMessage = letter;
               long[] letterSend = {letterMessage, random.nextLong()};
               encryption.encrypt(letterSend, key);
               outputStream.writeLong(letterSend[0]);
               outputStream.writeLong(letterSend[1]);
           }
          
           
          }
        } catch (IOException e) {
            System.out.println("Error");
            return;
        }
	}
		
}
